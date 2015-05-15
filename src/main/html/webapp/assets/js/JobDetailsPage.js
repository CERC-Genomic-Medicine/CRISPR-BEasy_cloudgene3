// Job Details Model
JobDetails = can.Model.extend({
	findOne : 'POST jobs/details',
	update : 'POST jobs/{action}'
}, {});

// Job Details Page Controller

JobDetailsPage = can.Control({

	// load details from database and rendering

	"init" : function(element, options) {
		var that = this;
		this.active = true;

		JobDetails.findOne({
			id : options.jobId
		}, function(job) {

			that.element.html(can.view('views/job-details.ejs', {
				job : job,
				results : options.results
			}));
			that.job = job;
			that.refresh();

		}, function(message) {
			new ErrorPage(that.element, {
				status : message.statusText,
				message : message.responseText
			});
		}

		)
	},

	// delete job

	'#btn-delete click' : function(el, ev) {
		var that = this;
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>"
				+ that.job.attr('id') + "</b>?", function(result) {
			if (result) {

				$("a[data-handler='1']").button('loading');
				$("a[data-handler='0']").hide('hide');

				that.job.attr('action', 'delete')
				that.job.save(function() {
					// go to jobs page
					bootbox.hideAll();
					window.location.hash = "!pages/jobs";
				}, function(message) {
					// show error message
					new ErrorPage(that.element, {
						status : message.statusText,
						message : message.responseText
					});
				});

				return false;

			}
		});

	},

	// cancel job

	'#btn-cancel click' : function(el, ev) {
		var that = this;
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to cancel <b>"
				+ that.job.attr('id') + "</b>?", function(result) {
			if (result) {

				$("a[data-handler='1']").button('loading');
				$("a[data-handler='0']").hide('hide');
				that.job.attr('action', 'cancel')
				that.job.save(function() {
					bootbox.hideAll();

					window.location.hash = "!pages/jobs";
				}, function(message) {
					// show error message
					new ErrorPage(that.element, {
						status : message.statusText,
						message : message.responseText
					});
				});

				return false;

			}
		});

	},

	'#btn-restart click' : function(el, ev) {
		var that = this;
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to restart <b>"
				+ that.job.attr('id') + "</b>?", function(result) {
			if (result) {

				$("a[data-handler='1']").button('loading');
				$("a[data-handler='0']").hide('hide');
				that.job.attr('action', 'restart')
				that.job.save(function() {
					bootbox.hideAll();

					window.location.hash = "!pages/jobs";
				}, function(message) {
					// show error message
					new ErrorPage(that.element, {
						status : message.statusText,
						message : message.responseText
					});
				});

				return false;

			}
		});
	},

	// refresh if job is running

	refresh : function() {
		var that = this;
		Job.findOne({
			job_id : that.job.id
		}, function(currentJob) {
			that.job.attr('state', currentJob.attr('state'));
			that.job.attr('executionTime', currentJob.attr('executionTime'));
			that.job.attr('map', currentJob.attr('map'));
			that.job.attr('reduce', currentJob.attr('reduce'));
			that.job.attr('steps', currentJob.attr('steps'));

			// needs refresh
			if (JobDetailsPage.needsUpdate(currentJob) && that.active) {
				setTimeout(function() {
					that.refresh();
				}, 5000);
			} else {
				// updates details (results, startTime, endTime, ...)
				JobDetails.findOne({
					id : that.job.id
				}, function(job) {

					if (this.active) {

						that.element.html(can.view('views/job-details.ejs', {
							job : job
						}));
					}

				}, function(message) {

					new ErrorPage(that.element, {
						status : message.statusText,
						message : message.responseText
					});

				});

			}

		});

	},

	destroy : function() {

		this.active = false;

		can.Control.prototype.destroy.call(this);
	}

});

JobDetailsPage.needsUpdate = function(job) {

	return job.attr("state") == 1 || job.attr("state") == 2
			|| job.attr("state") == 3;
}