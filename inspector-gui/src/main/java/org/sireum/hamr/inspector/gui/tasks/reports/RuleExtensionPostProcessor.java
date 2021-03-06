/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//package org.sireum.hamr.inspector.gui.tasks.reports;
//
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.junit.jupiter.api.extension.TestInstancePostProcessor;
//
//import java.util.Optional;
//
//// generated by junit5, do not throw exceptions as junit will swallow some (see ThrowableCollector, BlacklistedExceptions)
//@Slf4j
//@NoArgsConstructor // <-- important for generated junit5 class
//public class RuleExtensionPostProcessor implements TestInstancePostProcessor {
//
////    private static final Logger log = LoggerFactory.getLogger(RuleExtensionPostProcessor.class);
//
//    /**
//     * Will be called once per TestFactory, do NOT call task.getNextJob() here or it will offset for the tests.
//     * @param testInstance
//     * @param context
//     */
//    @Override
//    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
//        final RuleTest ruleTest = (RuleTest) testInstance;
//
//        warnIfNotNull(ruleTest.getMsgService(), "ruleTest should have no msgService because it is the job of this post-processing class to assign the msgService");
//
//        final Optional<String> configurationParameter =
//                context.getConfigurationParameter(GenerateTestReportTask.CONFIGURATION_PARAMETER_KEY);
//
//        if (configurationParameter.isEmpty()) {
//            log.error("test case should have configuration parameter pointing to test set, but it was not found");
//        }
//
//        final String id = configurationParameter.get();
//        final GenerateTestReportTask task = GenerateTestReportTask.getInstanceById(id);
//
//        final var job = task.getNextJob(); // getNextJob() may be called only ONCE per call to this method
//        warnIfNotNull(job, "ruleTest should have no ruleTestJobs because it is the job of this post-processing class to assign the ruleTestJobs");
//
//        // populate RuleTest's fields
//        ruleTest.setMsgService(task.msgService);
//        ruleTest.setJob(job);
//        ruleTest.setArtUtils(task.artUtils);
//    }
//
//    private static void warnIfNotNull(Object object, String msg) {
//        if (object != null) {
//            log.warn(msg);
//        }
//    }
//
//}



package org.sireum.hamr.inspector.gui.tasks.reports;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.Optional;

// generated by junit5, do not throw exceptions as junit will swallow some (see ThrowableCollector, BlacklistedExceptions)
@Slf4j
@NoArgsConstructor // <-- important for generated junit5 class
public class RuleExtensionPostProcessor implements TestInstancePostProcessor {

    /**
     * Will be called once per TestFactory, do NOT call task.getNextJob() here or it will offset for the tests.
     * @param testInstance
     * @param context
     * @throws Exception
     */
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        log.debug("Post-processing of RuleTest's dynamicTestFactory started.");
        final RuleTest ruleTest = (RuleTest) testInstance;

        warnIfNotNull(ruleTest.getMsgService(), "ruleTest should have no msgService because it is the job of this post-processing class to assign the msgService");
        warnIfNotNull(ruleTest.getJobs(), "ruleTest should have no ruleTestJobs because it is the job of this post-processing class to assign the ruleTestJobs");

        final Optional<String> configurationParameter =
                context.getConfigurationParameter(GenerateTestReportTask.CONFIGURATION_PARAMETER_KEY);

        if (configurationParameter.isEmpty()) {
            log.error("test case should have configuration parameter pointing to test set, but it was not found");
        }

        final String id = configurationParameter.get();
        final GenerateTestReportTask reportGenTask = GenerateTestReportTask.getInstanceById(id);

        // populate RuleTest's fields
        ruleTest.setMsgService(reportGenTask.msgService);
        ruleTest.setArtUtils(reportGenTask.artUtils);
        ruleTest.setJobs(reportGenTask.getJobs());
//        ruleTest.setLatch(reportGenTask.getLatch());
        log.debug("Post-processing of RuleTest's dynamicTestFactory complete.");
    }

    private static void warnIfNotNull(Object object, String msg) {
        if (object != null) {
            log.warn(msg);
        }
    }

}
