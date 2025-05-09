package cloudgene.mapred.server.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.ParameterDao;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.auth.AuthenticationType;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.server.services.DownloadService;
import cloudgene.mapred.server.services.JobService;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class DownloadController {

	protected static final Logger log = LoggerFactory.getLogger(DownloadController.class);

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected DownloadService downloadService;

	@Inject
	protected JobService jobService;

	@Get("/downloads/{jobId}/{hash}/{filename:.+}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public MutableHttpResponse<InputStream> downloadExternalResults(String jobId, String hash, String filename)
			throws URISyntaxException, IOException {

		AbstractJob job = jobService.getById(jobId);

		DownloadDao dao = new DownloadDao(application.getDatabase());
		Download download = dao.findByHash(hash);

		// job is running and not in database
		if (download == null) {
			download = job.findDownloadByHash(hash);
		}
		String message = String.format("Job: Downloading file '%s' for job %s", filename, job.getId());
		log.info(message);
		return downloadService.download(download);

	}

	@Get("/share/results/{hash}/{filename:.+}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public MutableHttpResponse<InputStream> downloadPublicLink(String hash, String filename)
			throws URISyntaxException, IOException {

		DownloadDao dao = new DownloadDao(application.getDatabase());
		Download download = dao.findByHash(hash);

		String message = String.format("Job: Anonymously downloading file '%s' (hash %s)", filename, hash);
		log.info(message);
		try {
			return downloadService.download(download);
		} catch (IOException e) {
			log.error("Downloading file failed.", e);
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "File not found in workspace.");
		}
	}

	@Get("/browse/{hash}/{filename:.+}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public MutableHttpResponse<InputStream> downloadByParamHash(String hash, String filename)
			throws URISyntaxException, IOException {

		ParameterDao parameterDao = new ParameterDao(application.getDatabase());
		CloudgeneParameterOutput param = parameterDao.findByHash(hash);

		if (param == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Param for hash " + hash + " not found.");
		}

		DownloadDao dao = new DownloadDao(application.getDatabase());
		Download download = dao.findByParameterAndName(param, filename);

		String message = String.format("Job: Anonymously downloading file '%s' (hash %s)", filename, hash);
		log.info(message);
		return downloadService.download(download);

	}

	@Get("/get/{hash}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String downloadScript(String hash) {

		ParameterDao parameterDao = new ParameterDao(application.getDatabase());
		CloudgeneParameterOutput param = parameterDao.findByHash(hash);

		if (param == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Param for hash " + hash + " not found.");
		}

		DownloadDao dao = new DownloadDao(application.getDatabase());
		List<Download> downloads = dao.findAllByParameter(param);

		String hostname = application.getSettings().getServerUrl();
		hostname += application.getSettings().getBaseUrl();

		StringBuffer script = new StringBuffer();
		script.append("#!/bin/bash\n");
		script.append("set -e\n");
		script.append("GREEN='\033[0;32m'\n");
		script.append("NC='\033[0m'\n");
		int i = 1;
		for (Download download : downloads) {
			script.append("echo \"\"\n");
			script.append(
					"echo \"Downloading file " + download.getName() + " (" + i + "/" + downloads.size() + ")...\"\n");
			script.append("curl -L " + hostname + "/share/results/" + download.getHash() + "/" + download.getName()
					+ " -o " + download.getName() + " --create-dirs \n");
			i++;
		}
		script.append("echo \"\"\n");
		script.append("echo -e \"${GREEN}All " + downloads.size() + " file(s) downloaded.${NC}\"\n");
		script.append("echo \"\"\n");
		script.append("echo \"\"\n");
		return script.toString();

	}

	@Get("/api/v2/jobs/{jobId}/chunks/{filename}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public File downloadChunk(Authentication authentication, String jobId, String filename) {

		User user = authenticationService.getUserByAuthentication(authentication, AuthenticationType.ALL_TOKENS);

		AbstractJob job = jobService.getByIdAndUser(jobId, user);

		String resultFile = FileUtil.path(application.getSettings().getLocalWorkspace(), job.getId(), "chunks",
				filename);

		return new File(resultFile);

	}

}
