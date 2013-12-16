package org.grails.plugins.elasticsearch.conversion.unmarshall

import grails.plugin.spock.IntegrationSpec
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.text.StringAndBytesText
import org.elasticsearch.search.internal.InternalSearchHit
import org.elasticsearch.search.internal.InternalSearchHits

class DomainClassUnmarshallerIntegrationSpec extends IntegrationSpec {


    def elasticSearchContextHolder

    def cleanupSpec() {
        def dataFolder = new File('data')
        if (dataFolder.isDirectory()) {
            dataFolder.deleteDir()
        }
    }

    void "An unmarshalled geo_point is marshalled into a GeoPoint domain object"() {
        def unmarshaller = new DomainClassUnmarshaller(elasticSearchContextHolder: elasticSearchContextHolder)

        given: 'a search hit with a geo_point'
        InternalSearchHit[] hits = [new InternalSearchHit(0, '1', new StringAndBytesText('building'), new BytesArray('{"location":{"lat":53.0,"lon":10.0},"name":"WatchTower"}'), [:])]
        def maxScore = 0.1534264087677002f
        def totalHits = 1
        def searchHits = new InternalSearchHits(hits, totalHits, maxScore)

        when: 'an geo_point is unmarshalled'
        def results = unmarshaller.buildResults(searchHits)
        results.size() == 1

        then: 'this results in a GeoPoint domain object'
        results[0].name == "WatchTower"
        def location = results[0].location
        location.class == 'test.GeoPoint'
        location.lat == 53.0
        location.lon == 10.0
    }
}
