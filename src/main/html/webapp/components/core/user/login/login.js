import Control from "can-control";
import $ from "jquery";

import ErrorPage from "helpers/error-page";
import template from "./login.stache";

export default Control.extend({
  init: function (element, options) {
    $(element).hide();
    $(element).html(
      template({
        oauth: options.appState.attr("oauth"),
      }),
    );
    $(element).fadeIn();
  },

  submit: function (element, event) {
    event.preventDefault();

    // var password = $(element).find("[name='password']");

    const serializedData = $(element).find("#signin-form").serialize(); // Example: "username=admin&password=admin1978"

    // Parse the serialized data into an object
    const params = new URLSearchParams(serializedData);

    // Extract the username and password
    const username = params.get("username");
    const password = params.get("password");

    if (username == "Public") {
      password.addClass("is-invalid");
      password
        .closest(".form-group")
        .find(".invalid-feedback")
        .html("Login Failed! Wrong Username or Password."); // Public is a protected username
    }

    $.ajax({
      url: "login?source=UI",
      type: "POST",
      data: $(element).find("#signin-form").serialize(),
      dataType: "json",
      success: function (response) {
        console.log($(element).find("#signin-form").serialize());
        var dataToken = {
          csrf: response.csrf,
          token: response.access_token,
        };
        localStorage.setItem("cloudgene", JSON.stringify(dataToken));

        var redirect = "./";
        window.location = redirect;
      },
      error: function (response) {
        console.log(response);
        password.addClass("is-invalid");
        password
          .closest(".form-group")
          .find(".invalid-feedback")
          .html(response.responseJSON.message);
      },
    });
  },
});
