package com.kleedy.registrocivil.validator

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Created by josebovet on 3/24/16.
 */
class DocumentRequest {

    @NotNull
    @Size(min = 10)
    String run

    @NotNull
    @Size(min = 1)
    String docType

    String docNumber
}
