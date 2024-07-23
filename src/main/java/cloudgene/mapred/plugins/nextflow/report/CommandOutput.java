package cloudgene.mapred.plugins.nextflow.report;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Step;
import cloudgene.mapred.plugins.nextflow.report.GitHubActionsParser.Command;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CommandOutput {

	public static final String DEFAULT_FILENAME = ".command.out";

	private List<Command> commands = new Vector<Command>();

	public CommandOutput() {

	}

	public CommandOutput(StringBuilder output) throws IOException {
		String str = output.toString();
		byte[] bytes = str.getBytes();
		loadFromInputStream(new ByteArrayInputStream(bytes));
	}

	public CommandOutput(String filename) throws IOException {
		loadFromFile(filename);
	}

	public CommandOutput(InputStream in) throws IOException {
		loadFromInputStream(in);
	}

	public void loadFromFile(String filename) throws IOException {
		loadFromInputStream(new FileInputStream(filename));
	}

	public void loadFromInputStream(InputStream in) throws IOException {
		GitHubActionsParser parser = new GitHubActionsParser();
		commands = parser.parseOutput(in);
	}

	public void execute(CloudgeneContext context, Step step) throws IOException {

		if (step == null) {
			step = context.getCurrentStep();
		}

		for (Command command : commands) {
			switch(command.getName()){
				case "error":
					context.message(step, command.getParameters().get("value"), CloudgeneContext.ERROR);
					break;
				case "warning":
					context.message(step, command.getParameters().get("value"), CloudgeneContext.WARNING);
					break;
				case "message":
				case "notice":
					context.message(step, command.getParameters().get("value"), CloudgeneContext.OK);
					break;
				case "log":
					context.log(command.getParameters().get("value"));
					break;
				case "debug":
					context.println(command.getParameters().get("value"));
					break;
				case "set-counter":
				case "inc-counter":
					context.incCounter(command.getParameters().get("name"), Integer.parseInt(command.getParameters().get("value")));
					break;
				case "submit-counter":
					context.submitCounter(command.getParameters().get("name"));
					break;
				case "set-value":
					context.setValue(command.getParameters().get("name"), command.getParameters().get("value"));
					break;
				case "submit-value":
					context.submitValue(command.getParameters().get("name"));
					break;
				default:
					throw new IOException("Unknown command: " + command.getName());
			}
		}
	}


}
