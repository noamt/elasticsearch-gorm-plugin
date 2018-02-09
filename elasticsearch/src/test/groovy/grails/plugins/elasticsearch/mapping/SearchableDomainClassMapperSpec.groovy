package grails.plugins.elasticsearch.mapping

import grails.core.GrailsApplication
import grails.test.mixin.Mock
import org.grails.core.DefaultGrailsDomainClass
import spock.lang.Specification
import test.Building
import test.File
import test.Product

@Mock(Product)
class SearchableDomainClassMapperSpec extends Specification {

    void 'a domain class with mapping geoPoint: true is mapped as a geo_point'() {
        ConfigObject config = [:] as ConfigObject
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for Building'

        def clazz = new DefaultGrailsDomainClass(Building)
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, clazz, config)

        when: 'the mapping is built'
        def classMapping = mapper.buildClassMapping()

        then: 'the location is mapped as a geoPoint'
        classMapping.domainClass == clazz
        classMapping.elasticTypeName == 'building'
        def locationMapping = classMapping.propertiesMapping.find { it.propertyName == 'location' }
        locationMapping.isGeoPoint()
    }

    void 'a domain class with mapping attachment: true is mapped as attachment'() {
        ConfigObject config = [:] as ConfigObject
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for File'

        def clazz = new DefaultGrailsDomainClass(File)
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, clazz, config)

        when: 'the mapping is built'
        def classMapping = mapper.buildClassMapping()

        then: 'the attachment property is mapped as attachment'
        classMapping.domainClass == clazz
        classMapping.elasticTypeName == 'file'
        def locationMapping = classMapping.propertiesMapping.find { it.propertyName == 'attachment' }
        locationMapping.isAttachment()
    }

    void 'the correct mapping is passed to the ES server'() {
        ConfigObject config = new ConfigObject()
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for Building'

        def clazz = new DefaultGrailsDomainClass(Building)
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, clazz, config)
        def classMapping = mapper.buildClassMapping()
        def mapping = ElasticSearchMappingFactory.getElasticMapping(classMapping)
        mapping == [
                building: [
                        properties: [
                                location: [
                                        type          : 'geo_point',
                                        include_in_all: true
                                ]
                        ]
                ]
        ]
    }

    void 'a mapping can be built from a domain class'() {
        ConfigObject config = [:] as ConfigObject
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for a domain class'
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, new DefaultGrailsDomainClass(Product), config)

        expect: 'a mapping can be built from this domain class'
        mapper.buildClassMapping()
    }

    void 'a mapping is aliased'() {
        ConfigObject config = [:] as ConfigObject
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for Building'

        def clazz = new DefaultGrailsDomainClass(Building)
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, clazz, config)

        when: 'the mapping is built'
        def classMapping = mapper.buildClassMapping()

        then: 'the date is aliased'
        classMapping.domainClass == clazz
        classMapping.elasticTypeName == 'building'
        def aliasMapping = classMapping.propertiesMapping.find { it.propertyName == 'date' }
        aliasMapping.isAlias()
    }

    void 'can get the mapped alias'() {
        ConfigObject config = [:] as ConfigObject
        GrailsApplication grailsApplication = [:] as GrailsApplication

        given: 'a mapper for Building'

        def clazz = new DefaultGrailsDomainClass(Building)
        SearchableDomainClassMapper mapper = new SearchableDomainClassMapper(grailsApplication, clazz, config)

        when: 'the mapping is built'
        def classMapping = mapper.buildClassMapping()

        then: 'the date is aliased'
        classMapping.domainClass == clazz
        classMapping.elasticTypeName == 'building'
        def aliasMapping = classMapping.propertiesMapping.find { it.propertyName == 'date' }
        aliasMapping.getAlias() == "@timestamp"
    }
}
