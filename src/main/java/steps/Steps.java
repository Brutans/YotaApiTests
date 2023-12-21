package steps;


import steps.apiSteps.ApiSteps;


public interface Steps {
    ApiSteps USER_STEPS = new ApiSteps();

    ApiSteps ADMIN_STEPS = new ApiSteps();
}
