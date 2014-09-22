package net.kaleidos.grails.plugin.security.stateless

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import org.apache.commons.lang.WordUtils

class SecurityStatelessFilters {

    def statelessService

    private boolean isSecuredStateless(String controllerName, String actionName, grailsApplication){
        def controller = grailsApplication.controllerClasses.find{controllerName == WordUtils.uncapitalize(it.name)}
        if (controller) {
            def clazz = controller.clazz
            if (clazz.isAnnotationPresent(SecuredStateless)) {
                return true
            }
            def method = clazz.methods.find{actionName == it.name}
            if (method) {
                return method.isAnnotationPresent(SecuredStateless)
            }
        }
        return false
    }


   def filters = {
        statelessFilter(controller:'*', action:'*') {
            before = {
                if (!isSecuredStateless(controllerName, actionName, grailsApplication)) {
                    return
                }

                def authorization = request.getHeader("Authorization")
                def map = statelessService.validateAndExtractToken(authorization)
                if (map) {
                    request.securityStatelessMap = map
                    return true
                }

                response.status = 401
                return false
            }
        }
   }
}
