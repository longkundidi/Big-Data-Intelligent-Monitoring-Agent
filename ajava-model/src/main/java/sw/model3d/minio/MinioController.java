package sw.model3d.minio;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.Inner;
import io.minio.ObjectWriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/minio")
@Tag(name = "minio文件接口")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MinioController {

	@Autowired
	private MinioUtil client;

	@Autowired
	private MinioServiceImpl minioServiceImpl;

	@Value("${minio.endpoint}")
	private String modelServerURL;

	@Operation(summary = "查看存储bucket是否存在", description = "查看存储bucket是否存在")
	@Inner(value = false)
	@GetMapping("/bucketExists")
	public R bucketExists() {
		try {
			if (client.bucketExists("modelfile"))
				return R.ok(null, "桶存在");
			else
				return R.ok(null, "桶不存在");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "桶查询失败");
		}
	};

	@Operation(summary = "创建存储bucket", description = "创建存储bucket")
	@Inner(value = false)
	@GetMapping("/createBucket/{bucketName}")
	public R createBucket(@PathVariable String bucketName) {
		try {
			if (!client.bucketExists(bucketName))
				client.createBucket(bucketName);
			return R.ok(bucketName, "桶创建成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "桶创建失败");
		}
	};

	@Operation(summary = "删除存储bucket", description = "删除存储bucket")
	@Inner(value = false)
	@GetMapping("/removeBucket")
	public R removeBucket() {
		try {
			client.removeBucket("modelfile");
			return R.ok(null, "桶删除成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "桶删除失败");
		}
	};

	@Operation(summary = "删除文件对象", description = "删除文件对象")
	@Inner(value = false)
	@GetMapping("/removeObject")
	public R removeObject(@RequestParam String modelUrl, @RequestParam String imgUrl) {
		int res = minioServiceImpl.removeModelAndImage(modelUrl, imgUrl);
		if (res == 0)
			return R.ok(res, "模型/图片删除成功");
		else if (res == 1)
			return R.failed(res, "模型删除失败");
		else if (res == 2)
			return R.failed(res, "图片删除失败");
		else
			return R.failed(res, "没有找到模型的文件和图片");
	};

	@Operation(summary = "Vue前端文件上传", description = "Vue前端文件上传")
	@Inner(value = false)
	@PostMapping("/uploadFile/{bucketName}")
	public R uploadFile(MultipartFile file, @PathVariable String bucketName) {
		try {
			ObjectWriteResponse res = client.uploadFile(bucketName, file, file.getOriginalFilename());
			return R.ok(modelServerURL + "/" + res.bucket() + "/" + res.object(), "文件上传成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "文件上传失败");
		}
	};

	@Operation(summary = "本地文件上传", description = "本地文件上传")
	@Inner(value = false)
	@GetMapping("/uploadLocalFile")
	public R uploadLocalFile() {
		try {
			ObjectWriteResponse res = client.uploadLocalFile("modelfile", "H:\\temp\\wifi.txt",
					client.getDatePath() + "wifi.txt");
			return R.ok(null, "文件上传成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "文件上传失败");
		}
	};

	@Operation(summary = "Vue前端文件下载", description = "Vue前端文件下载")
	@Inner(value = false)
	@GetMapping("/downloadFile")
	public R downloadFile(HttpServletResponse response) {
		try {
			client.downloadFile("modelimg", "车轴测试_1663496888278.png", response);
			return R.ok(null, "文件下载成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "文件下载失败");
		}
	}

	@Operation(summary = "下载文件到本地", description = "下载文件到本地")
	@Inner(value = false)
	@PostMapping("/downLocalFile")
	public R downLocalFile() {
		try {
			client.downLocalFile("modelfile", "车轴测试.glb", "H:\\temp\\test.glb");
			return R.ok(null, "文件下载成功");
		}
		catch (Exception e) {
			e.printStackTrace();
			return R.failed(null, "文件下载失败");
		}
	}

	@Operation(summary = "删除文件对象", description = "删除文件对象")
	@Inner(value = false)
	@GetMapping("/deleteObject")
	public R deleteObject(@RequestParam String bucketName, @RequestParam String fileUrl) {
		int res = minioServiceImpl.removeFile(bucketName, fileUrl);
		if (res == 0)
			return R.ok(res, "文件删除成功");
		else
			return R.failed(res, "文件删除失败");
	};

}
