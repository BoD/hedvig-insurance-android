plugins {
  id("hedvig.android.apollo")
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.adapters)
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)

  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreMarkdown)
}

apollo { // Octopus client
  service("octopus") {
    introspection {
      endpointUrl.set("https://apollo-router.dev.hedvigit.com")
      schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"))
    }
    schemaFiles.setFrom(
      file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"),
      file("src/main/graphql/com/hedvig/android/apollo/octopus/extra.graphqls"),
    )
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/octopus/graphql"))

    packageName.set("octopus")
    codegenModels.set(com.apollographql.apollo.compiler.MODELS_RESPONSE_BASED)

    generateApolloMetadata.set(true)
    generateDataBuilders.set(true)
    // https://slack-chats.kotlinlang.org/t/16051277/so-speaking-of-more-multi-module-stuff-i-got-a-query-like-th#976bd846-3cb0-4815-80a1-f73a853fe962
//    alwaysGenerateTypesMatching.set(
//      listOf(
//        "FirstVetSection",
//        "FirstVetAction",
//        "CrossSell",
//        "CrossSellType",
//        "MemberMutationOutput",
//        "Chat",
//        "ChatMessage",
//        "ContractCoInsured",
//        "ChatMessageSender",
//        "MemberChargeContractBreakdownItem",
//        "MemberCharge",
//        "MemberInsuranceCost",
//        "MemberReferral",
//        "MemberReferralInformation",
//        "MemberActions",
//      ),
//    )

    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables.set(false)
    outputDirConnection {
      connectToKotlinSourceSet("main") // main is by default but setting this explicitly fixed the warning "Duplicate content roots detected.
    }
    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.apollo.adapter.KotlinxLocalDateAdapter")
    mapScalar("DateTime", "kotlinx.datetime.Instant", "com.apollographql.apollo.adapter.KotlinxInstantAdapter")
    mapScalar("Instant", "kotlinx.datetime.Instant", "com.apollographql.apollo.adapter.KotlinxInstantAdapter")
    mapScalar(
      "Markdown",
      "com.hedvig.android.core.markdown.MarkdownString",
      "com.hedvig.android.apollo.octopus.MarkdownStringAdapter",
    )
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
  }
}
