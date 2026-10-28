package com.drivingschool.learningpackage.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class LearningPackageRepository
        extends FileRepository<LearningPackage> {

    public LearningPackageRepository(
            Path dataDirectory,
            ObjectMapper objectMapper) {

        super(
                dataDirectory,
                "packages.txt",
                LearningPackage.class,
                objectMapper
        );
    }
}
