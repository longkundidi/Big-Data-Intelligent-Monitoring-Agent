package sw.model3d.minio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sw.common.strUtils;

@Service
public class MinioServiceImpl {

	@Autowired
	private MinioUtil client;

	@Value("${minio.modelBucket}")
	private String modelBucket; // 模型桶名

	@Value("${minio.imageBucket}")
	private String imageBucket; // 图片桶名

	public int removeModelAndImage(String modelUrl, String imgUrl) {
		int res = -1;
		try {
			if (!"".equals(modelUrl)) {
				res = 1;
				res = client.removeObject(modelBucket, strUtils.getSubString("^/" + modelBucket + "/", modelUrl)); // 删除模型
			}
			if (!"".equals(imgUrl)) {
				res = 2;
				res = client.removeObject(imageBucket, strUtils.getSubString("^/" + imageBucket + "/", imgUrl)); // 删除图片
			}
			return res;
		}
		catch (Exception e) {
			e.printStackTrace();
			return res;
		}
	}

	/**
	 * @param bucketName: 桶名
	 * @param fileUrl: 数据库中文件URL地址（第一个部分是桶名）
	 * @return
	 */
	public int removeFile(String bucketName, String fileUrl) {
		int res = -1;
		try {
			if ((!"".equals(bucketName)) && (!"".equals(fileUrl))) {
				res = client.removeObject(bucketName, strUtils.getSubString("^/" + bucketName + "/", fileUrl)); // 删除模型
				res = 0;
			}
			return res;
		}
		catch (Exception e) {
			e.printStackTrace();
			return res;
		}
	}

}
