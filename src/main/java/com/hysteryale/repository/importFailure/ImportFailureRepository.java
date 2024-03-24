package com.hysteryale.repository.importFailure;

import com.hysteryale.model.importFailure.ImportFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportFailureRepository extends JpaRepository<ImportFailure, Integer> {
}
