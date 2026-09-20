package com.algorithm.web.service;

import com.algorithm.web.model.dto.PageModel;
import com.algorithm.web.model.dto.UploadFileDTO;
import com.algorithm.web.model.param.UploadFileParam;

public interface UploadFileService {

	void addFile(UploadFileDTO uploadFileDTO);

	void deleteFile(Long id);

	PageModel<UploadFileDTO> queryUploadFile(UploadFileParam uploadFileParam);

	UploadFileDTO getUploadFileByFileName(String fileName);

}
