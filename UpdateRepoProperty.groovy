import org.artifactory.repo.RepoPath
import org.artifactory.search.aql.AqlResult
import org.artifactory.repo.RepoPathFactory



// Example REST API calls:
// curl -X POST -v -u admin:Admin@123 "http://localhost:8082/artifactory/api/plugins/execute/updatePropertyValues?
// params=propertyName=artifact.developer;propertyValue=arunkgNew;repo=docker-local-akg"

executions {
    updatePropertyValues() { params ->
        propertyName = params?.get('propertyName')?.get(0) as String
        propertyValue = params?.get('propertyValue')?.get(0) as String
        repo = params?.get('repo')?.get(0) as String
        dryRun = new Boolean(params?.get('dryRun')?.get(0))
        updatePropertyValues(propertyName, propertyValue, repo, dryRun)
    }
}

private def updatePropertyValues(propertyName, propertyValue, repo, dryRun) {
    log.info "Searching for files that are missing the property $propertyName in $repo"

    def count = 0
    def aql = "items.find({\"repo\":{ \"\$eq\":\""+ repo +"\" }, \"property.key\":{ \"\$ne\":\"" + propertyName + "\"}})"

    searches.aql(aql) { AqlResult result ->
        result.each { Map item ->
            String itemPath = item.path + "/" + item.name
            log.info "Found: $itemPath"
            RepoPath repoPath = RepoPathFactory.create(repo, itemPath)

            log.info "Found: $itemPath"
            log.info "adding Property: $propertyName = $propertyValue"
            if (!dryRun) {
                repositories.setProperty(repoPath, propertyName, propertyValue)
            }
            count++
        }
    }

    if (count > 0) {
        log.info ("Succesfully tagged $count files (dryRun: $dryRun)")
    } else {
        log.info("All files is tagged with the property: '$propertyName' already. Did not do anything")
    }

    status = 200
}