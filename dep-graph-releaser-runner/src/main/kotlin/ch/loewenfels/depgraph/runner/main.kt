package ch.loewenfels.depgraph.runner

import ch.loewenfels.depgraph.data.maven.MavenProjectId
import ch.loewenfels.depgraph.maven.Analyser
import java.io.File

fun main(vararg args: String) {
    if (args.isEmpty()) {
        error("""
            |No arguments supplied
            |
            |$allCommands
        """.trimMargin())
    }

    when (args[0]) {
        "json" -> json(args)
        "html" -> html(args)
        else -> error("Unknown command supplied\n$allCommands")
    }
}

private const val JSON_GROUP_ID = 1
private const val JSON_ARTIFACT_ID = 2
private const val JSON_DIR = 3
private const val JSON_JSON = 4
private const val JSON_MISSING_PARENT_ANALYSIS = 5
const val MPOFF = "-mpoff"

private fun json(args: Array<out String>) {
    if (args.size < 5 || args.size > 6) {
        error("""
            |Not enough or too many arguments supplied for command: json
            |
            |$jsonArguments
            |
            |${getGivenArgs(args)}
            |
            |Following an example:
            |./produce json com.example example-project ./repo ./release.json
        """.trimMargin())
    }

    val turnMissingPartnerAnalysisOff = args.size == 6
    if (turnMissingPartnerAnalysisOff && args[JSON_MISSING_PARENT_ANALYSIS].toLowerCase() != MPOFF){
        error("""
            |Last argument supplied can only be $MPOFF for command: json
            |
            |$jsonArguments
            |
            |${getGivenArgs(args)}
            |
            |Following an example:
            |./produce json com.example example-project ./repo ./release.json -mpoff
        """.trimMargin())
    }

    val directoryToAnalyse = File(args[JSON_DIR])
    if (!directoryToAnalyse.exists()) {
        error("""
            |The given directory $directoryToAnalyse does not exist. Maybe you mixed up the order of the arguments?
            |
            |$jsonArguments
            |
            |${getGivenArgs(args)}
        """.trimMargin())
    }

    val json = File(args[JSON_JSON])
    if (!json.parentFile.exists()) {
        error("""The directory in which the resulting JSON file shall be created does not exists:
            |Directory: ${json.parentFile.canonicalPath}
        """.trimMargin())
    }
    val mavenProjectId = MavenProjectId(args[JSON_GROUP_ID], args[JSON_ARTIFACT_ID])
    val options = Analyser.Options(!turnMissingPartnerAnalysisOff)
    Orchestrator.analyseAndCreateJson(directoryToAnalyse, json, mavenProjectId, options)
}

private const val HTML_OUTPUT_DIR = 1

fun html(args: Array<out String>) {
    if (args.size != 2) {
        error("""
            |Not enough or too many arguments supplied for command: html
            |
            |$htmlArguments
            |
            |${getGivenArgs(args)}
            |
            |Following an example:
            |./produce html ./html
        """.trimMargin())
    }

    val outputDir = File(args[HTML_OUTPUT_DIR])
    if (!outputDir.exists()) {
        error("""The directory in which the resulting HTML file (and resources) shall be created does not exists:
            |Directory: ${outputDir.canonicalPath}
        """.trimMargin())
    }

    Orchestrator.copyResources(outputDir)
}

private fun getGivenArgs(args: Array<out String>) = "Given: ${args.joinToString(" ")}"

private val jsonArguments = """
|json requires the following arguments in the given order:
|
|groupId    // maven groupId of the project which shall be released
|artifactId // maven artifactId of the project which shall be released
|dir        // path to the directory where all projects are
|json       // path + file name for the resulting json file
|($MPOFF)   // optionally: turns missing parent analysis off
""".trimMargin()

private val htmlArguments = """
|html requires the following arguments in the given order:
|outDir     // path to the directory in which the html file and resources shall be created
""".trimMargin()

private val allCommands = """
|Currently we support the following commands:
|json       // analyse projects, create a release plan and serialize it to json
|html       // deserialize json and convert it to html
|
|$jsonArguments
|
|$htmlArguments
""".trimMargin()

private fun error(msg: String) = errorHandler.error(msg)

internal var errorHandler: ErrorHandler = object : ErrorHandler {
    override fun error(msg: String) {
        System.err.println(msg)
        System.exit(-1)
    }
}

internal interface ErrorHandler {
    fun error(msg: String)
}


