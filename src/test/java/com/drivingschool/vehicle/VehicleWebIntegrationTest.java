package com.drivingschool.vehicle;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.drivingschool.vehicle.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VehicleWebIntegrationTest {

    private static final String VEHICLES = "/admin/vehicles";
    private static final String CREATE = "/admin/vehicles/create";
    private static final String PASSWORD = "VehicleTestPass123!";

    @TempDir
    static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "app.data-directory",
                () -> dataDirectory.toString()
        );
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    AuthService authService;

    @Autowired
    AdminSeedService adminSeedService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    VehicleService vehicleService;

    @Test
    void anonymousUsersCannotAccessVehiclePagesOrSubmitChanges()
            throws Exception {

        MvcResult loginPage = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        String token = tokenOf(loginPage);
        MockHttpSession session = sessionOf(loginPage);

        mvc.perform(get(VEHICLES))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(CREATE))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", token)
                        .param("registrationNumber", "ABC1234")
                        .param("make", "Toyota")
                        .param("model", "Corolla")
                        .param("transmissionType", "MANUAL")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotAccessAdministratorVehiclePages()
            throws Exception {
        var student = createStudent();
        MockHttpSession session = login(student.getEmail());
        int vehicleCountBefore = vehicleRepository.readAllStrict().size();

        MvcResult home = mvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andReturn();

        String token = tokenOf(home);

        mvc.perform(get(VEHICLES).session(session))
                .andExpect(status().isForbidden());

        mvc.perform(get(CREATE).session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", token)
                        .param("registrationNumber", "ABC1234")
                        .param("make", "Toyota")
                        .param("model", "Corolla")
                        .param("transmissionType", "MANUAL")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().isForbidden());

        assertEquals(
                vehicleCountBefore,
                vehicleRepository.readAllStrict().size()
        );
    }

    @Test
    void administratorCanAccessListCreateAndEditPages()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        Vehicle vehicle = createVehicle(uniqueRegistration());

        mvc.perform(get(VEHICLES).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vehicle/list"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attributeExists("vehicles"));

        mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vehicle/create"))
                .andExpect(model().attributeExists(
                        "vehicleCreateForm",
                        "transmissionTypes",
                        "vehicleCategories",
                        "operationalStatuses",
                        "csrfToken"
                ));

        mvc.perform(get(VEHICLES + "/" + vehicle.getId() + "/edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vehicle/edit"))
                .andExpect(model().attributeExists(
                        "vehicle",
                        "vehicleEditForm",
                        "csrfToken"
                ));
    }

    @Test
    void successfulCreationPersistsVehicleAndRedirects()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        MvcResult page = mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", " ab-1234 ")
                        .param("make", " Toyota ")
                        .param("model", " Corolla ")
                        .param("transmissionType", "MANUAL")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(VEHICLES))
                .andExpect(flash().attribute("vehicleCreated", true))
                .andReturn();

        Vehicle stored = vehicleRepository.readAllStrict()
                .stream()
                .filter(vehicle ->
                        vehicle.getRegistrationNumber().equals("AB1234"))
                .findFirst()
                .orElseThrow();

        assertNotNull(stored.getId());
        assertEquals("Toyota", stored.getMake());
        assertEquals("Corolla", stored.getModel());
        assertEquals(TransmissionType.MANUAL, stored.getTransmissionType());
        assertEquals(VehicleCategory.CAR, stored.getVehicleCategory());
        assertEquals(
                VehicleOperationalStatus.IN_SERVICE,
                stored.getOperationalStatus()
        );

        mvc.perform(get(VEHICLES)
                        .session(session)
                        .flashAttrs(result.getFlashMap()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("vehicleCreated", true));
    }

    @Test
    void invalidCreationRedisplaysFormWithoutSaving()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        MvcResult page = mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk())
                .andReturn();

        int vehicleCountBefore = vehicleRepository.readAllStrict().size();

        MvcResult failed = mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", "BAD")
                        .param("make", "Toyota")
                        .param("model", "Corolla")
                        .param("transmissionType", "MANUAL")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("vehicle/create"))
                .andExpect(model().attributeExists(
                        "formError",
                        "csrfToken",
                        "vehicleCreateForm"
                ))
                .andReturn();

        assertEquals(
                "BAD",
                failed.getModelAndView().getModel()
                        .get("vehicleCreateForm") instanceof
                        com.drivingschool.vehicle.form.VehicleCreateForm form
                        ? form.getRegistrationNumber()
                        : null
        );

        assertEquals(
                vehicleCountBefore,
                vehicleRepository.readAllStrict().size()
        );
    }

    @Test
    void duplicateRegistrationNumberReturnsConflict()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        int vehicleCountBefore = vehicleRepository.readAllStrict().size();
        String existingRegistration = uniqueRegistration();

        createVehicle(existingRegistration);

        MvcResult page = mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult failed = mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", existingRegistration)
                        .param("make", "Honda")
                        .param("model", "Civic")
                        .param("transmissionType", "AUTOMATIC")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().isConflict())
                .andExpect(view().name("vehicle/create"))
                .andExpect(model().attributeExists(
                        "formError",
                        "csrfToken"
                ))
                .andReturn();

        assertEquals(
                vehicleCountBefore + 1,
                vehicleRepository.readAllStrict().size()
        );

        // A corrected registration number should be accepted.
        mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", tokenOf(failed))
                        .param("registrationNumber", uniqueRegistration())
                        .param("make", "Honda")
                        .param("model", "Civic")
                        .param("transmissionType", "AUTOMATIC")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(VEHICLES));

        assertEquals(
                vehicleCountBefore + 2,
                vehicleRepository.readAllStrict().size()
        );
    }

    @Test
    void successfulEditChangesOnlyEditableVehicleFields()
            throws Exception {

        MockHttpSession session = loginAsAdmin();
        Vehicle original = createVehicle(uniqueRegistration());

        MvcResult page = mvc.perform(
                        get(VEHICLES + "/" + original.getId() + "/edit")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();
        String updatedRegistration = uniqueRegistration();
        mvc.perform(post(VEHICLES + "/" + original.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", updatedRegistration)
                        .param("make", "Honda")
                        .param("model", "Civic"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(VEHICLES))
                .andExpect(flash().attribute("vehicleUpdated", true));

        Vehicle stored = vehicleRepository.findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals(
                updatedRegistration.toUpperCase(),
                stored.getRegistrationNumber()
        );
        assertEquals("Honda", stored.getMake());
        assertEquals("Civic", stored.getModel());

        assertEquals(
                original.getTransmissionType(),
                stored.getTransmissionType()
        );
        assertEquals(
                original.getVehicleCategory(),
                stored.getVehicleCategory()
        );
        assertEquals(
                original.getOperationalStatus(),
                stored.getOperationalStatus()
        );
    }

    @Test
    void submittedProtectedFieldsCannotChangeVehicleMetadata()
            throws Exception {

        MockHttpSession session = loginAsAdmin();
        Vehicle original = createVehicle(uniqueRegistration());

        MvcResult page = mvc.perform(
                        get(VEHICLES + "/" + original.getId() + "/edit")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();
        String updatedRegistration = uniqueRegistration();
        mvc.perform(post(VEHICLES + "/" + original.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", updatedRegistration)
                        .param("make", "Honda")
                        .param("model", "Civic")
                        .param("id", "V999")
                        .param("vehicleId", "V999")
                        .param("vehicleCategory", "MOTORCYCLE")
                        .param("transmissionType", "AUTOMATIC")
                        .param("operationalStatus", "RETIRED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(VEHICLES));

        Vehicle stored = vehicleRepository.findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals(original.getId(), stored.getId());
        assertEquals(
                updatedRegistration.toUpperCase(),
                stored.getRegistrationNumber()
        );
        assertEquals("Honda", stored.getMake());
        assertEquals("Civic", stored.getModel());

        assertEquals(
                original.getVehicleCategory(),
                stored.getVehicleCategory()
        );
        assertEquals(
                original.getTransmissionType(),
                stored.getTransmissionType()
        );
        assertEquals(
                original.getOperationalStatus(),
                stored.getOperationalStatus()
        );

        assertTrue(vehicleRepository.findById("V999").isEmpty());
    }

    @Test
    void invalidEditRedisplaysFormWithoutPersistingChanges()
            throws Exception {

        MockHttpSession session = loginAsAdmin();
        Vehicle original = createVehicle(uniqueRegistration());

        MvcResult page = mvc.perform(
                        get(VEHICLES + "/" + original.getId() + "/edit")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult failed = mvc.perform(
                        post(VEHICLES + "/" + original.getId() + "/edit")
                                .session(session)
                                .param("_csrf", tokenOf(page))
                                .param("registrationNumber", "BAD")
                                .param("make", "Honda")
                                .param("model", "Civic"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("vehicle/edit"))
                .andExpect(model().attributeExists(
                        "formError",
                        "csrfToken",
                        "vehicle",
                        "vehicleEditForm"
                ))
                .andReturn();

        Vehicle stored = vehicleRepository.findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals(original.getRegistrationNumber(), stored.getRegistrationNumber());
        assertEquals(original.getMake(), stored.getMake());
        assertEquals(original.getModel(), stored.getModel());

        assertNotNull(failed.getModelAndView());
    }

    @Test
    void duplicateRegistrationNumberOnEditReturnsConflict()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        String firstRegistration = uniqueRegistration();
        String secondRegistration = uniqueRegistration();

        Vehicle first = createVehicle(firstRegistration);
        Vehicle second = createVehicle(secondRegistration);

        MvcResult page = mvc.perform(
                        get(VEHICLES + "/" + first.getId() + "/edit")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post(VEHICLES + "/" + first.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("registrationNumber", second.getRegistrationNumber())
                        .param("make", "Honda")
                        .param("model", "Civic"))
                .andExpect(status().isConflict())
                .andExpect(view().name("vehicle/edit"))
                .andExpect(model().attributeExists(
                        "formError",
                        "csrfToken",
                        "vehicle"
                ));

        assertEquals(
                firstRegistration.toUpperCase(),
                vehicleRepository.findByIdStrict(first.getId())
                        .orElseThrow()
                        .getRegistrationNumber()
        );
    }

    @Test
    void invalidAndMissingVehicleIdsAreRejected()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        mvc.perform(get(VEHICLES + "/invalid/edit").session(session))
                .andExpect(status().isBadRequest());

        mvc.perform(get(VEHICLES + "/V000/edit").session(session))
                .andExpect(status().isBadRequest());

        mvc.perform(get(VEHICLES + "/V999/edit").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingAndInvalidCsrfTokensCannotCreateOrUpdateVehicles()
            throws Exception {

        MockHttpSession session = loginAsAdmin();

        int vehicleCountBefore = vehicleRepository.readAllStrict().size();
        Vehicle vehicle = createVehicle(uniqueRegistration());

        MvcResult createPage = mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult editPage = mvc.perform(
                        get(VEHICLES + "/" + vehicle.getId() + "/edit")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult foreignPage = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        String validCreateToken = tokenOf(createPage);
        String validEditToken = tokenOf(editPage);

        mvc.perform(post(CREATE)
                        .session(session)
                        .param("registrationNumber", "XYZ5678")
                        .param("make", "Honda")
                        .param("model", "Civic")
                        .param("transmissionType", "MANUAL")
                        .param("vehicleCategory", "CAR")
                        .param("operationalStatus", "IN_SERVICE"))
                .andExpect(status().isForbidden());

        mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", "invalid-token")
                        .param("registrationNumber", "XYZ5678"))
                .andExpect(status().isForbidden());

        mvc.perform(post(VEHICLES + "/" + vehicle.getId() + "/edit")
                        .session(session)
                        .param("_csrf", "invalid-token")
                        .param("registrationNumber", "XYZ5678")
                        .param("make", "Honda")
                        .param("model", "Civic"))
                .andExpect(status().isForbidden());

        mvc.perform(post(VEHICLES + "/" + vehicle.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(foreignPage))
                        .param("registrationNumber", "XYZ5678")
                        .param("make", "Honda")
                        .param("model", "Civic"))
                .andExpect(status().isForbidden());

        mvc.perform(post(CREATE)
                        .session(session)
                        .param("_csrf", validCreateToken, validCreateToken)
                        .param("registrationNumber", "XYZ5678"))
                .andExpect(status().isForbidden());

        mvc.perform(post(VEHICLES + "/" + vehicle.getId() + "/edit")
                        .session(session)
                        .param("_csrf", validEditToken, validEditToken)
                        .param("registrationNumber", "XYZ5678"))
                .andExpect(status().isForbidden());

        Vehicle stored = vehicleRepository.findByIdStrict(vehicle.getId())
                .orElseThrow();

        assertEquals(vehicle.getRegistrationNumber(), stored.getRegistrationNumber());
        assertEquals(
                vehicleCountBefore + 1,
                vehicleRepository.readAllStrict().size()
        );
    }

    private Vehicle createVehicle(String registrationNumber) {
        return vehicleService.createVehicle(
                registrationNumber,
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );
    }

    private com.drivingschool.student.model.Student createStudent() {
        return authService.registerStudent(
                "Vehicle Test Student",
                email(),
                "0771234567",
                PASSWORD
        );
    }

    private String email() {
        return "vehicle-" + UUID.randomUUID() + "@example.com";
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        User existingAdmin = userRepository.readAllStrict()
                .stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .findFirst()
                .orElse(null);

        String adminEmail;

        if (existingAdmin == null) {
            adminEmail = email();

            assertTrue(adminSeedService.seedIfNeeded(
                    "Vehicle Test Admin",
                    adminEmail,
                    PASSWORD
            ));
        } else {
            adminEmail = existingAdmin.getEmail();
        }

        return login(adminEmail);
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult page = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mvc.perform(post("/login")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("email", email)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        return sessionOf(result);
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView()
                .getModel()
                .get("csrfToken");
    }

    private String uniqueRegistration() {
        return "T" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 5)
                + "42";
    }
}