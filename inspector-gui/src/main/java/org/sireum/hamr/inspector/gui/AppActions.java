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

package org.sireum.hamr.inspector.gui;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceDialog;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.sireum.hamr.inspector.common.ArtUtils;
import org.sireum.hamr.inspector.common.Filter;
import org.sireum.hamr.inspector.common.Injection;
import org.sireum.hamr.inspector.common.Rule;
import org.sireum.hamr.inspector.engine.ServiceBeans;
import org.sireum.hamr.inspector.gui.modules.arch.ArchTab;
import org.sireum.hamr.inspector.gui.modules.console.ConsoleTab;
import org.sireum.hamr.inspector.gui.modules.msc.MscTab;
import org.sireum.hamr.inspector.gui.modules.rules.RulesTab;
import org.sireum.hamr.inspector.gui.tasks.reports.GenerateTestReportTask;
import org.sireum.hamr.inspector.gui.tasks.reports.RuleTestJob;
import org.sireum.hamr.inspector.services.InjectionService;
import org.sireum.hamr.inspector.services.MsgService;
import org.sireum.hamr.inspector.services.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import scala.collection.JavaConverters;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Lazy
@Slf4j
//@Import({AppDiscovery.class, ServiceBeans.class}) // unneeded with current setup order
@Configuration
public class AppActions {

    private final AppLoader appLoader;

    private final MsgService msgService;
    private final InjectionService injectionService;

    private final ArtUtils artUtils;

    private final ServiceBeans serviceBeans;

    private final ObservableList<Session> sessions;

    private final ObservableList<Rule> rules;
    private final ObservableList<Filter> filters;

    private final ObservableList<Injection> injections;

    public AppActions(AppLoader appLoader,
                      MsgService msgService,
                      InjectionService injectionService,
                      @Qualifier("sessions") ObservableList<Session> sessions,
                      @Qualifier("rules") ObservableList<Rule> rules,
                      @Qualifier("filters") ObservableList<Filter> filters,
                      @Qualifier("injections") ObservableList<Injection> injections,
                      ServiceBeans serviceBeans,
                      ArtUtils artUtils) {
        this.msgService = msgService;
        this.injectionService = injectionService;
        this.sessions = sessions;
        this.appLoader = appLoader;
        this.rules = rules;
        this.filters = filters;
        this.injections = injections;
        this.serviceBeans = serviceBeans;
        this.artUtils = artUtils;
    }

    @Lazy @Bean(name = "refreshSessionsAction")
    public Action refreshSessionsAction() {
        return new Action("Refresh Sessions List", event -> {
            log.info("Refreshing sessions list.");
            serviceBeans.refreshSessionsList();
        });
    }

    @Lazy @Bean(name = "runInjectionAction")
    public Action runInjectionAction() {
        return new Action("Run Injection", event -> {
            if (!injections.isEmpty() && !sessions.isEmpty()) {
                final var injectionChooserDialog = new ChoiceDialog<>(injections.get(0), injections);
                final var sessionChooserDialog = new ChoiceDialog<>(sessions.get(0), sessions);

                final Optional<Injection> injection = injectionChooserDialog.showAndWait();
                if (injection.isPresent()) {
                    final Optional<Session> session = sessionChooserDialog.showAndWait();
                    //noinspection OptionalIsPresent
                    if (session.isPresent()) {
                        injectionService.inject(session.get(), injection.get());
                    }
                }
            }
        });
    }

    @Lazy @Bean(name = "archTabAction")
    public Action archTabAction() {
        return new Action("New Arch Tab", event -> appLoader.loadAndInsertTab("arch", ArchTab.class));
    }

    @Lazy @Bean(name = "consoleTabAction")
    public Action consoleTabAction() {
        return new Action("New Console Tab", event -> appLoader.loadAndInsertTab("console", ConsoleTab.class));
    }

    @Lazy @Bean(name = "mscTabAction")
    public Action mscTabAction() {
        return new Action("New Msc Tab", event -> appLoader.loadAndInsertTab("msc", MscTab.class));
    }

    @Lazy @Bean(name = "rulesTabAction")
    public Action rulesTabAction() {
        return new Action("New Rules Tab", event -> appLoader.loadAndInsertTab("rules", RulesTab.class));
    }

    @Lazy @Bean(name = "generateTestReportAction")
    public Action generateTestReportAction() {
        return new Action("Generate Test Report", event -> {
            final Flux<RuleTestJob> loneTestCases = Flux.fromIterable(rules)
                    .flatMap(rule -> Flux.fromIterable(sessions).map(session -> new RuleTestJob(rule, session)));

            final Flux<RuleTestJob> pairedCases = Flux.fromIterable(filters)
                    .flatMapIterable(filter -> JavaConverters.asJavaCollection(filter.rules()))
                    .flatMap(rule -> Flux.fromIterable(sessions).map(session -> new RuleTestJob(rule, session)));

            final List<RuleTestJob> testCases = Objects.requireNonNullElseGet(
                        Flux.concat(loneTestCases, pairedCases).collectList().block(),
                        Collections::emptyList);

            final var task = new GenerateTestReportTask(testCases, msgService, artUtils);

            Notifications.create()
                    .title("Generate Test Report Task Created")
                    .graphic(new MaterialDesignIconView(MaterialDesignIcon.FILE_DOCUMENT))
                    .hideAfter(javafx.util.Duration.seconds(5))
                    .owner(appLoader.getRootNode())
                    .show();

            Schedulers.boundedElastic().schedule(task);
            appLoader.getAppNode().taskProgressView.getTasks().add(task);
        });
    }
}
