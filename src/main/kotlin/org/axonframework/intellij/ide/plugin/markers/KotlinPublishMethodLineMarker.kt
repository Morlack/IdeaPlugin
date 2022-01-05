package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.axonframework.intellij.ide.plugin.search.HandlerSearcher
import org.jetbrains.kotlin.idea.debugger.sequence.psi.resolveType
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class KotlinPublishMethodLineMarker: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if(element.parent !is KtNameReferenceExpression || element.parent.parent !is KtCallExpression) {
            return null
        }

        val resolvedType = (element.parent.parent as KtCallExpression).resolveType()
        val fq = resolvedType.fqName ?: return null
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val type = JavaPsiFacade.getInstance(project).elementFactory.createTypeByFQClassName(fq.asString(), scope)
        val handlers = HandlerSearcher.findAllMessageHandlers(project).filter { it.payload.isAssignableFrom(type) }
        if(handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.AxonIconOut)
                .setTooltipText("Navigate to Axon handlers")
                .setTargets(handlers.map { it.method })
                .createLineMarkerInfo(element)
    }
}
