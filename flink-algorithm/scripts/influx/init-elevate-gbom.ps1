[CmdletBinding()]
param(
    [string]$InfluxUrl = 'http://192.168.16.219:8086',
    [string]$InfluxOrg = 'bigdata',
    [string]$InfluxBucket = 'elevate',
    [Parameter(Mandatory = $true)]
    [string]$InfluxToken,
    [string]$MySqlHost = '127.0.0.1',
    [int]$MySqlPort = 3306,
    [string]$MySqlDatabase = 'new_algorithom_Repository',
    [string]$MySqlUser = 'root',
    [string]$MySqlPassword = 'root'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function ConvertTo-InfluxTagValue {
    param([AllowNull()][string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return 'unknown'
    }

    return $Value.Replace('\', '\\').Replace(',', '\,').Replace(' ', '\ ').Replace('=', '\=')
}

function ConvertTo-InfluxMeasurement {
    param([AllowNull()][string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return 'unknown'
    }

    return $Value.Replace('\', '\\').Replace(',', '\,').Replace(' ', '\ ')
}

function ConvertTo-InfluxFieldKey {
    param([AllowNull()][string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return 'unknown'
    }

    return $Value.Replace('\', '\\').Replace(',', '\,').Replace(' ', '\ ').Replace('=', '\=')
}

$sql = @'
SELECT
  project.project,
  project.product_model,
  root_node.node_name AS elevator_instance,
  node.turbine_code,
  node.node_id,
  node.node_code,
  node.node_name,
  node.node_level,
  node.node_type,
  GROUP_CONCAT(ancestor.node_name ORDER BY ancestor.node_level SEPARATOR '/') AS full_path
FROM config_bom_tree node
JOIN al_resume_data project
  ON project.id = CAST(node.pro_id AS UNSIGNED)
 AND project.bom_model = 'SBOM'
JOIN config_bom_tree root_node
  ON root_node.turbine_code = node.turbine_code
 AND root_node.node_level = 1
LEFT JOIN config_bom_tree ancestor
  ON ancestor.turbine_code = node.turbine_code
 AND (ancestor.node_code = node.node_code OR node.node_code LIKE CONCAT(ancestor.node_code, '-%'))
WHERE root_node.node_name LIKE '电梯%'
   OR node.turbine_code LIKE '%电梯%'
GROUP BY
  project.project,
  project.product_model,
  root_node.node_name,
  node.turbine_code,
  node.node_id,
  node.node_code,
  node.node_name,
  node.node_level,
  node.node_type
ORDER BY project.project, project.product_model, node.turbine_code, node.node_level, node.node_code;
'@

$previousPassword = $env:MYSQL_PWD
$env:MYSQL_PWD = $MySqlPassword
try {
    $rows = & mysql `
        --host=$MySqlHost `
        --port=$MySqlPort `
        --user=$MySqlUser `
        --database=$MySqlDatabase `
        --default-character-set=utf8mb4 `
        --batch `
        --raw `
        --skip-column-names `
        --execute=$sql
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL GBOM query failed with exit code $LASTEXITCODE"
    }
}
finally {
    if ($null -eq $previousPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
    else {
        $env:MYSQL_PWD = $previousPassword
    }
}

if (-not $rows) {
    throw 'No SBOM GBOM nodes were found; catalog was not written.'
}

$catalogDateUtc = [DateTime]::SpecifyKind([DateTime]::UtcNow.Date, [DateTimeKind]::Utc)
$timestamp = ([DateTimeOffset]$catalogDateUtc).ToUnixTimeSeconds()
$lineProtocol = foreach ($row in $rows) {
    $columns = $row -split "`t", 10
    if ($columns.Count -lt 10) {
        throw "Unexpected GBOM row: $row"
    }

    $scene = $columns[0]
    $deviceCategory = $columns[1]
    $elevatorInstance = $columns[2]
    $elevatorCode = $columns[3]
    $nodeId = $columns[4]
    $nodeCode = $columns[5]
    $nodeName = $columns[6]
    $nodeLevel = [int]$columns[7]
    $nodeType = $columns[8]
    $fullPath = $columns[9]

    if ($fullPath -eq $elevatorInstance) {
        $gbomPath = '/'
    }
    elseif ($fullPath.StartsWith($elevatorInstance + '/')) {
        $gbomPath = $fullPath.Substring($elevatorInstance.Length + 1)
    }
    else {
        $gbomPath = $fullPath
    }

    $tags = [System.Collections.Generic.List[string]]::new()
    $tags.Add('elevator_instance=' + (ConvertTo-InfluxTagValue $elevatorInstance))

    if ($gbomPath -ne '/') {
        $gbomSegments = $gbomPath -split '/'
        for ($index = 0; $index -lt $gbomSegments.Count; $index++) {
            $gbomLevel = $index + 2
            $tags.Add("gbom_level_$gbomLevel=" + (ConvertTo-InfluxTagValue $gbomSegments[$index]))
        }
    }

    $measurement = ConvertTo-InfluxMeasurement $scene
    $fieldKey = ConvertTo-InfluxFieldKey $deviceCategory
    "$measurement,$($tags -join ',') $fieldKey=${nodeLevel}i $timestamp"
}

$headers = @{ Authorization = 'Token ' + $InfluxToken }
$writeUri = "$($InfluxUrl.TrimEnd('/'))/api/v2/write?org=$([uri]::EscapeDataString($InfluxOrg))&bucket=$([uri]::EscapeDataString($InfluxBucket))&precision=s"
$body = $lineProtocol -join "`n"

Invoke-WebRequest `
    -Uri $writeUri `
    -Headers $headers `
    -Method Post `
    -ContentType 'text/plain; charset=utf-8' `
    -Body $body `
    -TimeoutSec 30 | Out-Null

Write-Host "Initialized $($lineProtocol.Count) scene/device/elevator/GBOM hierarchy points in bucket '$InfluxBucket'."
