import request from "@/router/axios";
export function hasDuplicateFieldValues(array) {
    let result = new Set();

    array.forEach(subArray => {
        const nodeCodes = subArray.map(obj => obj.node_code);
        const uniqueNodeCodes = new Set(nodeCodes);
        const duplicates = new Set(nodeCodes.filter(code => !uniqueNodeCodes.delete(code)));

        if (duplicates.size > 0) {
            result.add(subArray[0].node_name);
        }
    });

    return Array.from(result);
}

export function updateUnitData(data){
    return request({
        url: `/model3d/configmodel/updateUnitData`,
        method:"post",
        data:data
    })
}
