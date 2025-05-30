package org.rookedsysc.cqrsreplicarag.config;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@Service
@Transactional(readOnly = true)
public @interface QueryService {}
