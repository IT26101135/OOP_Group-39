// Module owner: IT26101132
package com.drivingschool.vehicle.api;

import com.drivingschool.vehicle.model.Vehicle;

import java.util.List;
import java.util.Optional;

/** The vehicle storage operations used by application services. */
public interface VehicleStore {

    String nextId(String prefix);

    void save(Vehicle vehicle);

    List<Vehicle> readAll();

    List<Vehicle> readAllStrict();

    Optional<Vehicle> findById(String id);

    Optional<Vehicle> findByIdStrict(String id);

    void update(Vehicle vehicle);

    void delete(String id);
}
