package grails.plugins.elasticsearch.compiler

import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.grails.compiler.injection.GrailsASTUtils

class DomainSearchableTypeCheckingExtension  extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {
	@Override
	public Object run() {
		setup { newScope() }

		finish { scopeExit() }

		beforeVisitClass { ClassNode classNode ->
			def mappingProperty = classNode.getField('searchable')
			if(mappingProperty && mappingProperty.isStatic() && mappingProperty.initialExpression instanceof ClosureExpression) {
				def sourceUnit = classNode?.module?.context
				if(GrailsASTUtils.isDomainClass(classNode, sourceUnit)) {
					newScope {
						searchableClosureCode = mappingProperty.initialExpression.code
					}
					mappingProperty.initialExpression.code = new EmptyStatement()
				}
			}
		}

		afterVisitClass { ClassNode classNode ->
			if(currentScope.searchableClosureCode) {
				def mappingProperty = classNode.getField('searchable')
				mappingProperty.initialExpression.code = currentScope.searchableClosureCode
				currentScope.checkingSearchableClosure = true
				withTypeChecker { visitClosureExpression mappingProperty.initialExpression }
				scopeExit()
			}
		}

		methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
			def dynamicCall
			if(currentScope.searchableClosureCode && currentScope.checkingSearchableClosure) {
				dynamicCall = makeDynamic (call)
			}
			dynamicCall
		}

		null
	}
}
