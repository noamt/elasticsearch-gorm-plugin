package grails.plugins.elasticsearch.compiler

import groovy.transform.AnnotationCollector
import groovy.transform.CompileStatic

@AnnotationCollector
@CompileStatic(extensions=['org.grails.compiler.ValidateableTypeCheckingExtension',
	'org.grails.compiler.HttpServletRequestTypeCheckingExtension',
	'org.grails.compiler.WhereQueryTypeCheckingExtension',
	'org.grails.compiler.DynamicFinderTypeCheckingExtension',
	'org.grails.compiler.DomainMappingTypeCheckingExtension',
	'grails.plugins.elasticsearch.compiler.DomainSearchableTypeCheckingExtension',
	'org.grails.compiler.RelationshipManagementMethodTypeCheckingExtension'])
@interface GrailsSearchableCompileStatic {
}
