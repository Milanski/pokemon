package com.mbls.challenge.pokemon;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "com.mbls.challenge.pokemon", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String[] FRAMEWORK_PACKAGES = {
            "org.springframework..",
            "jakarta.persistence..",
            "jakarta.servlet..",
            "org.hibernate..",
    };

    private static final String TRAINER_CONTEXT = "com.mbls.challenge.pokemon.trainer..";
    private static final String CATALOG_CONTEXT = "com.mbls.challenge.pokemon.catalog..";
    private static final String COLLECTION_CONTEXT = "com.mbls.challenge.pokemon.collection..";
    private static final String COLLECTION_CATALOG_BRIDGE = "com.mbls.challenge.pokemon.collection.adapter.out.catalog..";

    @ArchTest
    static final ArchRule domain_layer_is_framework_free = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES)
            .because("the domain model must be understandable and testable without Spring, JPA or a servlet container");

    @ArchTest
    static final ArchRule application_layer_is_framework_free = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES)
            .because("application services are wired as plain objects via explicit @Bean methods "
                    + "precisely so they stay framework-free and unit-testable with `new`, "
                    + "not @Service/@Component");

    @ArchTest
    static final ArchRule collection_only_reaches_catalog_through_its_bridge_adapter = noClasses()
            .that().resideInAPackage(COLLECTION_CONTEXT)
            .and().resideOutsideOfPackage(COLLECTION_CATALOG_BRIDGE)
            .should().dependOnClassesThat().resideInAPackage(CATALOG_CONTEXT)
            .because("collection expresses its need for Pokémon data through its own "
                    + "PokemonCatalogLookupPort; only CatalogPokemonLookupAdapter may know catalog exists");

    @ArchTest
    static final ArchRule collection_does_not_depend_on_trainer = noClasses()
            .that().resideInAPackage(COLLECTION_CONTEXT)
            .should().dependOnClassesThat().resideInAPackage(TRAINER_CONTEXT)
            .because("collection identifies a collection's owner only by the shared-kernel TrainerId, "
                    + "never by trainer's own domain model");

    @ArchTest
    static final ArchRule trainer_is_self_contained = noClasses()
            .that().resideInAPackage(TRAINER_CONTEXT)
            .should().dependOnClassesThat().resideInAnyPackage(CATALOG_CONTEXT, COLLECTION_CONTEXT)
            .because("trainer is a fully self-contained identity bounded context");

    @ArchTest
    static final ArchRule catalog_is_self_contained = noClasses()
            .that().resideInAPackage(CATALOG_CONTEXT)
            .should().dependOnClassesThat().resideInAnyPackage(TRAINER_CONTEXT, COLLECTION_CONTEXT)
            .because("catalog is a one-way anti-corruption layer around PokéAPI: other contexts may "
                    + "depend on it (through their own ports), but it must never depend back");
}
