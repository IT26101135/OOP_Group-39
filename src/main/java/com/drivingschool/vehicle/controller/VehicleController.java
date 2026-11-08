// Module owner: IT26101132
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
import com.drivingschool.vehicle.service.VehicleService;
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

    private final VehicleService vehicleService;
    private final CsrfTokenService csrfTokenService;

    public VehicleController(
            VehicleService vehicleService,
            CsrfTokenService csrfTokenService) {
        this.vehicleService = vehicleService;
        this.csrfTokenService = csrfTokenService;
    }

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

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the vehicle information and try again."
            );
            return createPage(request, model);
        }

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
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        Vehicle vehicle = vehicleService.getVehicleByIdStrict(id);

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

        // Strictly load the target selected by the URL, not by submitted form data.
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

    @PostMapping("/{id}/delete")
    public String deleteVehicle(@PathVariable String id,
                                RedirectAttributes redirectAttributes) {
        vehicleService.deleteVehicle(id);
        redirectAttributes.addFlashAttribute("vehicleDeleted", true);
        return "redirect:/admin/vehicles";
    }

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

    private String editPage(
            HttpServletRequest request,
            Model model) {

        model.addAttribute("transmissionTypes", TransmissionType.values());
        model.addAttribute("vehicleCategories", VehicleCategory.values());
        model.addAttribute("operationalStatuses", VehicleOperationalStatus.values());
        addCsrfAttributes(request, model);

        return "vehicle/edit";
    }

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
