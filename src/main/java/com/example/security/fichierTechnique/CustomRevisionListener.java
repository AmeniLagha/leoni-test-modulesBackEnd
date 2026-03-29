package com.example.security.fichierTechnique;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {

        CustomRevisionEntity rev = (CustomRevisionEntity) revisionEntity;

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            rev.setUsername(auth.getName());
        } else {
            rev.setUsername("SYSTEM");
        }
    }
}
