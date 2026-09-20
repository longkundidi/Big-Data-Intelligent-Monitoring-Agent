package com.algorithm.web.service.impl.taskConfig;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.mapper.taskConfig.ServerListMapper;
import com.algorithm.web.model.entity.taskConfig.ServerList;
import com.algorithm.web.model.entity.taskConfig.vo.ServerVo;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.taskConfig.ServerListService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 记录所有服务器相关信息(ServerList)表服务实现类
 *
 * @author makejava
 * @since 2024-11-04 17:22:24
 */
@Service
public class ServerListServiceImpl extends ServiceImpl<ServerListMapper, ServerList> implements ServerListService {

	@Autowired
	private AlTaskService alTaskService;

	@Override
	public List<ServerVo> getList() {
		List<ServerList> serverLists = this.list();
		List<ServerVo> serverVos = new ArrayList<>();

		for (ServerList server : serverLists) {
			ServerVo serverVo = new ServerVo();
			serverVo.setId(server.getId());
			serverVo.setName(server.getName());
			serverVo.setUrl(server.getUrl());

			// 获取系统 CPU 和内存使用情况
			Map<String, Object> systemStats = getServerStatsViaSSH(server.getUrl(), "root", "1234qwer");
			double cpuUsage = (Double) systemStats.getOrDefault("cpuUsage", 0.0);
			BigDecimal cpuUsageBD = new BigDecimal(cpuUsage).setScale(2, RoundingMode.HALF_UP);
			serverVo.setCpuUsage(cpuUsageBD.doubleValue());

			double memoryUsage = (Double) systemStats.getOrDefault("memoryUsage", 0.0);
			BigDecimal memoryUsageBD = new BigDecimal(memoryUsage).setScale(2, RoundingMode.HALF_UP);
			serverVo.setMemoryUsage(memoryUsageBD.doubleValue());

			// 将总内存和剩余内存从 MB 转换为 GB
			long totalMemoryMB = (Long) systemStats.getOrDefault("totalMemory", 0L);
			long freeMemoryMB = (Long) systemStats.getOrDefault("freeMemory", 0L);

			double totalMemoryGB = totalMemoryMB / 1024.0;
			double freeMemoryGB = freeMemoryMB / 1024.0;

			BigDecimal totalMemoryGBBD = new BigDecimal(totalMemoryGB).setScale(2, RoundingMode.HALF_UP);
			BigDecimal freeMemoryGBBD = new BigDecimal(freeMemoryGB).setScale(2, RoundingMode.HALF_UP);

			serverVo.setTotalMemory(totalMemoryGBBD.doubleValue());
			serverVo.setFreeMemory(freeMemoryGBBD.doubleValue());

			serverVo.setIsRunning((Boolean) systemStats.getOrDefault("isRunning", false));

			Page page = new Page(); // 假设 Page 是一个已定义的类
			Page pageData = alTaskService.getPage(page, server.getName());
			if (pageData != null) {
				serverVo.setMicroservicesCount(pageData.getTotal());
			}

			serverVos.add(serverVo);
		}

		return serverVos;
	}

	public List<ServerVo> getListDefault() {
		List<ServerList> serverLists = this.list();
		List<ServerVo> serverVos = new ArrayList<>();

		for (ServerList server : serverLists) {
			ServerVo serverVo = new ServerVo();
			serverVo.setId(server.getId());
			serverVo.setName(server.getName());
			serverVo.setUrl(server.getUrl());
			if (serverVo.getName().equals("219服务器")) {

				serverVo.setTotalMemory(32.0); // 假设总内存为32GB
				serverVo.setFreeMemory(19.17); // 空闲内存
				serverVo.setMemoryUsage(12.83); // 已用内存 = 32 - 19.17
				serverVo.setCpuUsage(7.8); // 同 cpuPercent
				serverVo.setIsRunning(true); // 在线
				serverVo.setMicroservicesCount(15L); // 微服务个数
			}
			else if (serverVo.getName().equals("220服务器")) {

				serverVo.setTotalMemory(16.0); // 假设总内存为16GB
				serverVo.setFreeMemory(2.71); // 空闲内存
				serverVo.setMemoryUsage(13.29); // 已用内存
				serverVo.setCpuUsage(3.3); // 同 cpuPercent
				serverVo.setIsRunning(true); // 在线
				serverVo.setMicroservicesCount(8L); // 微服务个数
			}
			serverVos.add(serverVo);
		}

		return serverVos;
	}

	// 通过 SSH 获取系统 CPU 和内存使用情况
	private Map<String, Object> getServerStatsViaSSH(String host, String user, String password) {
		Map<String, Object> stats = new HashMap<>();
		boolean isRunning = true; // 默认假设服务器运行正常

		// 去掉 http:// 前缀
		if (host.startsWith("http://")) {
			host = host.substring("http://".length());
		}

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setPassword(password);

			// Disable StrictHostKeyChecking to avoid known_hosts file issues
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();

			// Execute the commands
			String command = "top -bn1 | grep 'Cpu(s)' && free -m";
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);

			InputStream in = channelExec.getInputStream();
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			boolean isCpuLine = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Cpu(s)")) {
					isCpuLine = true;
					Pattern pattern = Pattern.compile(
							"%Cpu\\(s\\):\\s*\\d+\\.\\d+\\sus,\\s*\\d+\\.\\d+\\ssy,\\s*\\d+\\.\\d+\\sni,\\s*(\\d+\\.\\d+)\\sid,.*");
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						double idle = Double.parseDouble(matcher.group(1));
						double usage = 100.0 - idle;
						stats.put("cpuUsage", usage);
					}
				}
				else if (isCpuLine && line.contains("Mem:")) {
					String[] parts = line.split("\\s+");
					long total = Long.parseLong(parts[1]);
					long used = Long.parseLong(parts[2]);
					long free = Long.parseLong(parts[3]);
					double usage = (double) used / total * 100.0;

					stats.put("memoryUsage", usage);
					stats.put("totalMemory", total);
					stats.put("freeMemory", free);
					break;
				}
			}

			channelExec.disconnect();
			session.disconnect();
		}
		catch (Exception e) {
			isRunning = false;
			e.printStackTrace();
		}

		stats.put("isRunning", isRunning);
		return stats;
	}

}
