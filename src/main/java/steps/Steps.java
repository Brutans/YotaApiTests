package steps;

import steps.apiSteps.AdminSteps;
import steps.apiSteps.UserSteps;


public interface Steps {
    UserSteps USER_STEPS = new UserSteps();

    AdminSteps ADMIN_STEPS = new AdminSteps();
}
