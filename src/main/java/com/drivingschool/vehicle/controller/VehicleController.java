/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleController.java
WHAT IT DOES (simple words): Accepts fleet forms and prepares the vehicle pages.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.vehicle.form.VehicleCreateForm;
import com.drivingschool.vehicle.form.VehicleEditForm;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.api.VehicleOperations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireAdmin
@RequestMapping("/admin/vehicles")
public class VehicleController {

    // SOLID: D - The controller receives the fleet interface through its constructor.
    private final VehicleOperations vehicleService;
    private final CsrfTokenService csrfTokenService;

    // Receives the details this class needs and keeps them ready for its own job.
    public VehicleController(
            VehicleOperations vehicleService,
            CsrfTokenService csrfTokenService) {
        this.vehicleService = vehicleService;
        this.csrfTokenService = csrfTokenService;
    }

    // Allows only known vehicle fields from the browser.
    @InitBinder("vehicleCreateForm")
    public void configureCreateBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "registrationNumber",
                "make",
                "model",
                "transmissionType",
                "vehicleCategory",
                "operationalStatus",
                "insuranceExpiry", "lastServiceDate", "nextServiceDate"
        );
        binder.setAutoGrowNestedPaths(false);
    }

    // Keeps the URL-selected identity separate from editable form fields.
    @InitBinder("vehicleEditForm")
    public void configureEditBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "registrationNumber",
                "make",
                "model", "transmissionType", "vehicleCategory", "operationalStatus",
                "insuranceExpiry", "lastServiceDate", "nextServiceDate"
        );
        binder.setAutoGrowNestedPaths(false);
    }

    // GET /admin/vehicles
    // Prepares fleet cards and safe form tokens for the admin page.
    // CRUD: READ   - finds or lists vehicles
    @GetMapping
    public String listVehicles(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        model.addAttribute(
                "vehicles",
                vehicleService.getAllVehicles()
        );
        addCsrfAttributes(request, model);

        return "vehicle/list";
    }

    // GET /admin/vehicles/create
    // Opens an empty vehicle form with the allowed type and status choices.
    @GetMapping("/create")
    public String showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        model.addAttribute(
                "vehicleCreateForm",
                new VehicleCreateForm()
        );

        return createPage(request, model);
    }

    // POST /admin/vehicles/create
    // Checks the new vehicle and prevents a duplicate registration before saving.
    // CRUD: CREATE - adds a new vehicle and saves it
    @PostMapping("/create")
    public String createVehicle(
            @ModelAttribute("vehicleCreateForm")
            VehicleCreateForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        // Step 1: Return the typed form when a date or choice could not be read.
        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the vehicle information and try again."
            );
            return createPage(request, model);
        }

        // Step 2: Let the fleet rules validate the details before saving.
        try {
            vehicleService.createVehicle(
                    form.getRegistrationNumber(),
                    form.getMake(),
                    form.getModel(),
                    form.getTransmissionType(),
                    form.getVehicleCategory(),
                    form.getOperationalStatus(), form.getInsuranceExpiry(),
                    form.getLastServiceDate(), form.getNextServiceDate()
            );

            // Step 3: Tell the administrator the new vehicle was saved.
            redirectAttributes.addFlashAttribute(
                    "vehicleCreated",
                    true
            );

            return "redirect:/admin/vehicles";

        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            return createPage(request, model);

        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            return createPage(request, model);
        }
    }

    // GET /admin/vehicles/{id}/edit
    // Fills the edit form from the vehicle chosen in the URL.
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        // Step 1: Load the URL-selected vehicle so the form starts with its saved details.
        Vehicle vehicle = vehicleService.getVehicleByIdStrict(id);

        // Step 2: Copy its editable details into the form without changing the saved vehicle.
        VehicleEditForm form = new VehicleEditForm();
        form.setRegistrationNumber(vehicle.getRegistrationNumber());
        form.setMake(vehicle.getMake());
        form.setModel(vehicle.getModel());
        form.setTransmissionType(vehicle.getTransmissionType());
        form.setVehicleCategory(vehicle.getVehicleCategory());
        form.setOperationalStatus(vehicle.getOperationalStatus());
        form.setInsuranceExpiry(vehicle.getInsuranceExpiry());
        form.setLastServiceDate(vehicle.getLastServiceDate());
        form.setNextServiceDate(vehicle.getNextServiceDate());

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleEditForm", form);

        return editPage(request, model);
    }

    // POST /admin/vehicles/{id}/edit
    // Checks a replacement record before changing the stored vehicle.
    // CRUD: UPDATE - changes an existing vehicle
    @PostMapping("/{id}/edit")
    public String updateVehicle(
            @PathVariable String id,
            @ModelAttribute("vehicleEditForm")
            VehicleEditForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        // Step 1: Load the target selected by the URL, not by submitted form data.
        Vehicle existingVehicle =
                vehicleService.getVehicleByIdStrict(id);

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the vehicle information and try again."
            );
            model.addAttribute("vehicle", existingVehicle);
            return editPage(request, model);
        }

        // Step 2: Validate a replacement before changing the existing fleet record.
        try {
            vehicleService.updateVehicle(
                    id,
                    form.getRegistrationNumber(),
                    form.getMake(),
                    form.getModel(),
                    form.getTransmissionType() == null ? existingVehicle.getTransmissionType() : form.getTransmissionType(),
                    form.getVehicleCategory() == null ? existingVehicle.getVehicleCategory() : form.getVehicleCategory(),
                    form.getOperationalStatus() == null ? existingVehicle.getOperationalStatus() : form.getOperationalStatus(),
                    form.getInsuranceExpiry() == null ? existingVehicle.getInsuranceExpiry() : form.getInsuranceExpiry(),
                    form.getLastServiceDate() == null ? existingVehicle.getLastServiceDate() : form.getLastServiceDate(),
                    form.getNextServiceDate() == null ? existingVehicle.getNextServiceDate() : form.getNextServiceDate()
            );

            // Step 3: Show success only after the checked replacement has been saved.
            redirectAttributes.addFlashAttribute(
                    "vehicleUpdated",
                    true
            );

            return "redirect:/admin/vehicles";

        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            model.addAttribute("vehicle", existingVehicle);
            return editPage(request, model);

        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            model.addAttribute("vehicle", existingVehicle);
            return editPage(request, model);
        }
    }

    // Keeps lesson history intact and removes only an unreferenced vehicle.
    // CRUD: DELETE - removes a vehicle
    @PostMapping("/{id}/delete")
    public String deleteVehicle(@PathVariable String id,
                                RedirectAttributes redirectAttributes) {
        vehicleService.deleteVehicle(id);
        redirectAttributes.addFlashAttribute("vehicleDeleted", true);
        return "redirect:/admin/vehicles";
    }

    // Keeps the entered values while showing the create form and allowed choices.
    private String createPage(
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "transmissionTypes",
                TransmissionType.values()
        );
        model.addAttribute(
                "vehicleCategories",
                VehicleCategory.values()
        );
        model.addAttribute(
                "operationalStatuses",
                VehicleOperationalStatus.values()
        );

        addCsrfAttributes(request, model);

        return "vehicle/create";
    }

    // Keeps the entered values while showing the edit form and allowed choices.
    private String editPage(
            HttpServletRequest request,
            Model model) {

        model.addAttribute("transmissionTypes", TransmissionType.values());
        model.addAttribute("vehicleCategories", VehicleCategory.values());
        model.addAttribute("operationalStatuses", VehicleOperationalStatus.values());
        addCsrfAttributes(request, model);

        return "vehicle/edit";
    }

    // Adds the session token that protects vehicle form submissions.
    private void addCsrfAttributes(
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "csrfParameterName",
                CsrfTokenService.PARAMETER_NAME
        );

        model.addAttribute(
                "csrfToken",
                csrfTokenService.getOrCreateToken(request)
        );
    }
}
