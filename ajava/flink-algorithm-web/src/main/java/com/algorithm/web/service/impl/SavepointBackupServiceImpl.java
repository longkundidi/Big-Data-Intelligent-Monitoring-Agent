package com.algorithm.web.service.impl;

import com.algorithm.web.mapper.SavepointBackupMapper;
import com.algorithm.web.model.entity.SavepointBackup;
import com.algorithm.web.service.SavepointBackupService;
import com.algorithm.web.model.dto.SavepointBackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-09-17
 * @time 20:26
 */
@Service
public class SavepointBackupServiceImpl implements SavepointBackupService {

	@Autowired
	private SavepointBackupMapper savepointBackupMapper;

	@Override
	public void insertSavepoint(Long jobConfigId, String savepointPath, Date backupTime) {
		SavepointBackup savepointBackup = new SavepointBackup();
		savepointBackup.setBackupTime(backupTime);
		savepointBackup.setSavepointPath(savepointPath);
		savepointBackup.setJobConfigId(jobConfigId);
		savepointBackupMapper.insert(savepointBackup);
	}

	@Override
	public List<SavepointBackupDTO> lasterHistory10(Long jobConfigId) {
		return SavepointBackupDTO.toDTOList(savepointBackupMapper.selectByLimt10(jobConfigId));
	}

	@Override
	public String getSavepointPathById(Long jobConfigId, Long id) {
		SavepointBackup savepointBackup = savepointBackupMapper.getSavepointBackupById(jobConfigId, id);
		if (savepointBackup != null) {
			return savepointBackup.getSavepointPath();
		}
		return null;
	}

}
