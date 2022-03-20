package cloudgene.mapred.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.TestApplication;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;
import cloudgene.sdk.internal.WorkflowContext;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class WorkflowEngineTest {

	@Inject
	TestApplication application;

	@Test
	public void testReturnTrueStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true.yaml");

		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	@Test
	public void testReturnFalseStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	@Test
	public void testReturnExceptionStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-exception.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	@Test
	public void testReturnTrueInSetupStep() throws Exception {
		
		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	@Test
	public void testReturnFalseInSetupStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	@Test
	public void testReturnTrueInSecondSetupStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	@Test
	public void testReturnTrueInSecondSetupStepAndNormalStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-true-in-setup3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// one steps
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	@Test
	public void testHiddenInputsAndDefaultValues() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/print-hidden-inputs.yaml");

		Map<String, String> inputs = new HashMap<String, String>();

		AbstractJob job = createJobFromWdl(app, inputs);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
		
		//check step ouputs
		assertEquals("text1: my-value\n",  job.getSteps().get(0).getLogMessages().get(0).getMessage());
		assertEquals("checkbox1: true\n",  job.getSteps().get(1).getLogMessages().get(0).getMessage());
		assertEquals("list1: value1\n",  job.getSteps().get(2).getLogMessages().get(0).getMessage());
		assertEquals("text2: my-value\n",  job.getSteps().get(3).getLogMessages().get(0).getMessage());
		assertEquals("checkbox2: true\n",  job.getSteps().get(4).getLogMessages().get(0).getMessage());
		assertEquals("list2: value1\n",  job.getSteps().get(5).getLogMessages().get(0).getMessage());
		
		
	}
	
	@Test
	public void testReturnWriteFileInSecondSetupStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		
		String myContent = "test-test-test-test-text";

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-file-in-setup.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", myContent);

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Settings settings = application.getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertEquals(myContent, content);

		app = WdlReader.loadAppFromFile("test-data/write-file-in-setup-failure.yaml");

		params = new HashMap<String, String>();
		params.put("inputtext", myContent);

		job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

		System.out.println("ok:" + job.getOutputParams().get(0).getValue());

		path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		filename = FileUtil.path(settings.getLocalWorkspace(), path);
		content = FileUtil.readFileAsString(filename);
		assertEquals(myContent, content);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);

		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// one steps
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);

	}

	@Test
	public void testEmptyStepList() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/no-steps.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "test");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());
	}

	@Test
	public void testReturnFalseInSecondSetupStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/return-false-in-setup2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());
	}

	@Test
	public void testWriteTextToFileJob() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Settings settings = application.getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_SUCCESS);
	}

	@Test
	public void testWriteTextToFileOnFailureInStepJob() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		Thread.sleep(4000);
		Settings settings = application.getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	@Test
	public void testWriteTextToFileOnFailureInSetupJob() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Thread.sleep(4000);

		Settings settings = application.getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// steps not executed
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	@Test
	public void testWriteTextToFileOnFailureInSecondSetupJob() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-file-on-failure3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("inputtext", "lukas_text");

		CloudgeneJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		Thread.sleep(4000);

		Settings settings = application.getSettings();
		String path = job.getOutputParams().get(0).getFiles().get(0).getPath();
		String filename = FileUtil.path(settings.getLocalWorkspace(), path);
		String content = FileUtil.readFileAsString(filename);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		// no steps executed
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals("lukas_text", content);
		assertEquals(job.getState(), AbstractJob.STATE_FAILED);
	}

	@Test
	public void testThreeTasksStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/three-tasks.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		List<Message> messages = job.getSteps().get(0).getLogMessages();

		assertEquals(3, messages.size());
		assertEquals("cloudgene-task1", messages.get(0).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(0).getType());
		assertEquals("cloudgene-task2", messages.get(1).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(1).getType());
		assertEquals("cloudgene-task3", messages.get(2).getMessage());
		assertEquals(WorkflowContext.OK, messages.get(2).getType());
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
	}

	@Test
	public void testWriteTextToStdOutStep() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/write-text-to-std-out.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "input-file");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		String stdout = FileUtil.path(application.getSettings().getLocalWorkspace(), job.getId(),
				"std.out");
		String contentStdOut = FileUtil.readFileAsString(stdout);

		String log = FileUtil.path(application.getSettings().getLocalWorkspace(), job.getId(), "job.txt");
		String contentlog = FileUtil.readFileAsString(log);

		assertTrue(contentStdOut.contains("taks write to system out"));
		assertTrue(contentStdOut.contains("taks write to system out2"));
		assertTrue(contentStdOut.contains("taks write to system out3"));

		assertTrue(contentlog.contains("taks write to log"));
		assertTrue(contentlog.contains("taks write to log2"));
		assertTrue(contentlog.contains("taks write to log3"));
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
	}

	@Test
	public void testApplicationLinks() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("property1:hey!"));
		assertTrue(message.getMessage().contains("property2:hey2!"));
		assertTrue(message.getMessage().contains("property3:hey3!"));

	}
	
	@Test
	public void testApplicationLinksWithoutAppsPrefix() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();

		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "app-links-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("property1:hey!"));
		assertTrue(message.getMessage().contains("property2:hey2!"));
		assertTrue(message.getMessage().contains("property3:hey3!"));

	}
	
	@Test
	public void testApplicationLinksWithoutVersion() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "app-links-child-version");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("property1:hey!"));
		assertTrue(message.getMessage().contains("property2:hey2!"));
		assertTrue(message.getMessage().contains("property3:hey3!"));

	}
	
	@Test
	public void testOptionalApplicationLinks() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links-optional.yaml");

		Map<String, String> params = new HashMap<String, String>();

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertFalse(message.getMessage().contains("property1:hey!"));
		assertFalse(message.getMessage().contains("property2:hey2!"));
		assertFalse(message.getMessage().contains("property3:hey3!"));

	}
	
	@Test
	public void testApplicationLinksWrongApplication() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child-wrong-id");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

	}

	@Test
	public void testApplicationLinksWrongPermissions() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-links.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("app", "apps@app-links-child-protected");

		AbstractJob job = createJobFromWdlAsUser(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}
		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() == 0);
		assertTrue(job.getEndTime() == 0);
		assertEquals(AbstractJob.STATE_FAILED, job.getState());

	}

	@Test
	public void testApplicationInstallation() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-installation.yaml");

		Map<String, String> params = new HashMap<String, String>();

		AbstractJob job = createJobFromWdl(app, params);
		job.forceInstallation(true);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		// single file
		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of metafile.txt"));

		// folder file1
		message = job.getSteps().get(1).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// folder file2
		message = job.getSteps().get(2).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// zip file1
		message = job.getSteps().get(3).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// zip file2
		message = job.getSteps().get(4).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// gz file1
		message = job.getSteps().get(5).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file1.txt"));

		// gz file2
		message = job.getSteps().get(6).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of file2.txt"));

		// http single file
		message = job.getSteps().get(7).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().contains("name: hello-cloudgene"));
	}

	@Test
	public void testApplicationInstallationAndLinks() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-installation2.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("dataset", "apps@app-installation-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

		Message message = job.getSteps().get(0).getLogMessages().get(0);
		assertEquals(Message.OK, message.getType());
		assertTrue(message.getMessage().equals("content of metafile2.txt"));
	}
	
	@Test
	public void testApplicationLinksAndPropertyList() throws Exception {

		WorkflowEngine engine = application.getWorkflowEngine();
		
		WdlApp app = WdlReader.loadAppFromFile("test-data/app-installation3.yaml");

		Map<String, String> params = new HashMap<String, String>();
		params.put("dataset", "apps@app-installation-child");

		AbstractJob job = createJobFromWdl(app, params);
		engine.submit(job);
		while (!job.isComplete()) {
			Thread.sleep(500);
		}

		assertTrue(job.getSubmittedOn() > 0);
		assertTrue(job.getFinishedOn() > 0);
		assertTrue(job.getSetupStartTime() > 0);
		assertTrue(job.getSetupEndTime() > 0);
		assertTrue(job.getStartTime() > 0);
		assertTrue(job.getEndTime() > 0);
		assertEquals(AbstractJob.STATE_SUCCESS, job.getState());

	}
	

	// TODO: merge and zip export.

	// TODO: write to hdfs temp and local temp (temp output params)!

	// TODO: check if removehdfsworkspace works!

	// TODO: check cloudgene counters (successful and failed)

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs) throws Exception {

		UserDao userDao = new UserDao(application.getDatabase());				
		User user = userDao.findByUsername("admin");

		return createJobFromWdl(app, inputs, user);
	}

	public CloudgeneJob createJobFromWdlAsUser(WdlApp app, Map<String, String> inputs) throws Exception {

		UserDao userDao = new UserDao(application.getDatabase());				
		User user = userDao.findByUsername("user");
		
		return createJobFromWdl(app, inputs, user);
	}

	public CloudgeneJob createJobFromWdl(WdlApp app, Map<String, String> inputs, User user) throws Exception {

		Settings settings = application.getSettings();

		String id = "test_" + System.currentTimeMillis();

		String hdfsWorkspace = HdfsUtil.path(settings.getHdfsWorkspace(), id);
		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		FileUtil.createDirectory(localWorkspace);

		CloudgeneJob job = new CloudgeneJob(user, id, app, inputs);
		job.setId(id);
		job.setName(id);
		job.setLocalWorkspace(localWorkspace);
		job.setHdfsWorkspace(hdfsWorkspace);
		job.setSettings(settings);
		job.setRemoveHdfsWorkspace(true);
		job.setApplication(app.getName() + " " + app.getVersion());
		job.setApplicationId(app.getId());

		return job;
	}

}
