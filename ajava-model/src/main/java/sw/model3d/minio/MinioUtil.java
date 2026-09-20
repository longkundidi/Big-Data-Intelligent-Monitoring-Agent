package sw.model3d.minio;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import org.apache.commons.io.IOUtils;
import sw.common.strUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MinioUtil {

	@Autowired
	private MinioClient minioClient;

	// 查看存储bucket是否存在
	public Boolean bucketExists(String bucket) throws Exception {
		return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
	}

	// 创建存储bucket
	public Boolean createBucket(String bucket) throws Exception {
		if (!bucketExists(bucket))
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
		return true;
	}

	// 删除存储bucket
	public Boolean removeBucket(String bucket) throws Exception {
		if (bucketExists(bucket))
			minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
		return true;
	}

	// Vue前端文件上传(文件名称相同会覆盖)
	public ObjectWriteResponse uploadFile(String bucket, MultipartFile file, String remoteFileName) throws Exception {
		String newFileName = strUtils.timestampFileName(remoteFileName); // 文件名添加时间戳
		return minioClient.putObject(PutObjectArgs.builder()
			.bucket(bucket)
			.stream(file.getInputStream(), file.getSize(), PutObjectArgs.MIN_MULTIPART_SIZE)
			.object(newFileName != null ? newFileName : remoteFileName)
			.contentType(file.getContentType())
			.build());
	}

	// 本地文件上传
	public ObjectWriteResponse uploadLocalFile(String bucket, String localFileName, String remoteFileName)
			throws Exception {
		File file = new File(localFileName);
		FileInputStream fileInputStream = new FileInputStream(file);
		return minioClient.putObject(PutObjectArgs.builder()
			.stream(fileInputStream, file.length(), PutObjectArgs.MIN_MULTIPART_SIZE)
			.object(remoteFileName)
			.bucket(bucket)
			.build());
	};

	// 下载文件写入HttpServletResponse
	public void downloadFile(String bucket, String remoteFileName, HttpServletResponse response) throws Exception {
		GetObjectResponse object = minioClient
			.getObject(GetObjectArgs.builder().bucket(bucket).object(remoteFileName).build());
		response.setHeader("Content-Disposition", "attachment;filename=" + remoteFileName);
		response.setContentType("application/force-download");
		response.setCharacterEncoding("UTF-8");
		IOUtils.copy(object, response.getOutputStream());
	}

	// 下载文件到本地
	public void downLocalFile(String bucket, String remoteFileName, String localFileName) throws Exception {
		minioClient.downloadObject(
				DownloadObjectArgs.builder().bucket(bucket).object(remoteFileName).filename(localFileName).build());
	}

	// 查看文件对象
	/*
	 * public List<ObjectItem> listObjects(String bucketName) { Iterable<Result<Item>>
	 * results = minioClient.listObjects(
	 * ListObjectsArgs.builder().bucket(bucketName).build()); List<ObjectItem> objectItems
	 * = new ArrayList<>(); try { for (Result<Item> result : results) { Item item =
	 * result.get(); ObjectItem objectItem = new ObjectItem();
	 * objectItem.setObjectName(item.objectName()); objectItem.setSize(item.size());
	 * objectItems.add(objectItem); } } catch (Exception e) { e.printStackTrace(); return
	 * null; } return objectItems; }
	 */

	/**
	 * 删除文件对象
	 * @param bucket: 桶名
	 * @param object: 对象名
	 * @return
	 * @throws Exception
	 */
	public int removeObject(String bucket, String object) throws Exception {
		minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(object).build());
		return 0;
	}

	// 批量删除文件对象
	public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
		List<DeleteObject> dos = objects.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
		Iterable<Result<DeleteError>> results = minioClient
			.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
		return results;
	}

	// https://blog.csdn.net/fzyjiangfeng/article/details/124589415
	// https://blog.csdn.net/qq_28834355/article/details/120484917

	// 按时间日期创建目录
	public String getDatePath() {
		LocalDateTime now = LocalDateTime.now();
		return String.format("/%s/%s/%s/", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
	}

}
