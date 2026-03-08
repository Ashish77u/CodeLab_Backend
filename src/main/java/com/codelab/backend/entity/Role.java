package com.codelab.backend.entity;

public enum Role {
    USER,
    ADMIN
}
// NOTE: Spring Security's hasRole("ADMIN") automatically
// prepends "ROLE_" internally. So store "ADMIN" in DB,
// not "ROLE_ADMIN". Don't double-prefix.