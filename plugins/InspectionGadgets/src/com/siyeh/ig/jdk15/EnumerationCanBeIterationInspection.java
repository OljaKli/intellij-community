/*
 * Copyright 2007 Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.jdk15;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class EnumerationCanBeIterationInspection extends BaseInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "enumeration.can.be.iteration.display.name");
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "enumeration.can.be.iteration.problem.descriptor",
                infos[0]);
    }

    @Nullable
    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new EnumerationCanBeIterationFix();
    }

    // TODO finish quick fix and enable inspection.
    private static class EnumerationCanBeIterationFix
            extends InspectionGadgetsFix {

        @NotNull
        public String getName() {
            return InspectionGadgetsBundle.message(
                    "enumeration.can.be.iteration.quickfix");
        }

        boolean foo(URL url) {
            try {
                return new URL("asdf").equals(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        protected void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiElement element = descriptor.getPsiElement();
            final PsiReferenceExpression methodExpression =
                    (PsiReferenceExpression)element.getParent();
            final PsiMethodCallExpression methodCallExpression =
                    (PsiMethodCallExpression)methodExpression.getParent();
            final PsiElement parent =
                    methodCallExpression.getParent();
            boolean deleteEnumerationVariable = true;
            final PsiVariable variable;
            if (parent instanceof PsiVariable) {
                variable = (PsiVariable) parent;
            } else if (parent instanceof PsiAssignmentExpression) {
                final PsiAssignmentExpression assignmentExpression =
                        (PsiAssignmentExpression) parent;
                final PsiExpression lhs = assignmentExpression.getLExpression();
                if (!(lhs instanceof PsiReferenceExpression)) {
                    return;
                }
                final PsiReferenceExpression referenceExpression =
                        (PsiReferenceExpression) lhs;
                final PsiElement target = referenceExpression.resolve();
                if (!(target instanceof PsiVariable)) {
                    return;
                }
                variable = (PsiVariable) target;
                if (!(variable instanceof PsiLocalVariable)) {
                    deleteEnumerationVariable = false;
                }
            } else {
                return;
            }
            final String variableName = createVariableName(element);
            final Query<PsiReference> query =
                    ReferencesSearch.search(variable, variable.getUseScope());
            final int elementOffset = element.getTextOffset();
            boolean checkReassignment = false;
            for (PsiReference reference : query) {
                final PsiElement referenceElement = reference.getElement();
//                if (elementOffset < referenceElement.getTextOffset()) {
//                    if (PsiUtil.isAccessedForWriting(referenceElement)) {
//                        break;
//                    }
//                }
                
                final PsiElement referenceParent = referenceElement.getParent();
                if (referenceParent instanceof PsiMethodCallExpression) {
                    if (elementOffset < referenceParent.getTextOffset()) {

                    }
                }
            }

            @NonNls final String methodName =
                    methodExpression.getReferenceName();
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            final PsiManager manager = element.getManager();
            final PsiElementFactory factory = manager.getElementFactory();
            if ("elements".equals(methodName)) {
                if (TypeUtils.expressionHasTypeOrSubtype(qualifier,
                        "java.util.Vector")) {
                    final String qualifierText;
                    if (qualifier == null) {
                        qualifierText = "";
                    } else {
                        qualifierText = qualifier.getText() + '.';
                    }
                    final PsiStatement newStatement =
                            factory.createStatementFromText("Iterator " +
                                    variableName + ' ' + qualifierText +
                                    "iterator()", element);
                    element.replace(newStatement);
                } else if (TypeUtils.expressionHasTypeOrSubtype(qualifier,
                    "java.util.Hashtable")) {

                }
            } else if ("keys".equals(methodName)) {
                if (TypeUtils.expressionHasTypeOrSubtype(qualifier,
                        "java.util.Hashtable")) {

                }
            }
        }

        private static String createVariableName(PsiElement context) {
            final PsiManager manager = context.getManager();
            final PsiElementFactory factory = manager.getElementFactory();
            final Project project = context.getProject();
            final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            final PsiClass iteratorClass =
                    manager.findClass("java.util.Iterator", scope);
            if (iteratorClass == null) {
                return "iterator";
            }
            final CodeStyleManager codeStyleManager =
                    manager.getCodeStyleManager();
            final PsiType iteratorType = factory.createType(iteratorClass);
            final SuggestedNameInfo nameInfo =
                    codeStyleManager.suggestVariableName(
                            VariableKind.LOCAL_VARIABLE, null, null,
                            iteratorType);
            final String variableName;
            if (nameInfo.names.length > 0) {
                variableName = nameInfo.names[0];
            } else {
                variableName = "iterator";
            }
            return variableName;
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new EnumerationCanBeIterationVisitor();
    }

    static void foo(Vector v, Hashtable h) {
        Enumeration e = v.elements();
        while (e.hasMoreElements()) {
            System.out.println(e.nextElement());
        }
        Iterator i = v.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
        e = h.elements();
        while (e.hasMoreElements()) {
            System.out.println(e.nextElement());
        }
        e = h.keys();
        i = h.values().iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }

    private static class EnumerationCanBeIterationVisitor
            extends BaseInspectionVisitor {

        @NonNls
        private static final String ITERATOR_TEXT = ".iterator()";

        @NonNls
        private static final String KEY_SET_ITERATOR_TEXT =
                ".keySet().iterator()";

        @NonNls
        private static final String VALUES_ITERATOR_TEXT =
                ".values().iterator()";

        public void visitMethodCallExpression(
                PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiElement parent = expression.getParent();
            if (!(parent instanceof PsiLocalVariable)) {
                return;
            }
            final PsiReferenceExpression methodExpression =
                    expression.getMethodExpression();
            @NonNls final String methodName =
                    methodExpression.getReferenceName();
            if (!TypeUtils.expressionHasTypeOrSubtype(expression,
                    "java.util.Enumeration")) {
                return;
            }
            final PsiVariable variable = (PsiVariable) parent;
            final PsiMethod containingMethod = PsiTreeUtil.getParentOfType(
                    variable, PsiMethod.class);
            if (containingMethod == null) {
                return;
            }
            if (isEnumerationMethodCalled(variable, containingMethod)) {
                return;
            }
            if ("elements".equals(methodName)) {
                final PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }
                final PsiClass containingClass = method.getContainingClass();
                if (ClassUtils.isSubclass(containingClass,
                        "java.util.Vector")) {
                    registerMethodCallError(expression, ITERATOR_TEXT);
                } else if (ClassUtils.isSubclass(containingClass,
                        "java.util.Hashtable")) {
                    registerMethodCallError(expression, VALUES_ITERATOR_TEXT);
                }
            } else if ("keys".equals(methodName)) {
                final PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }
                final PsiClass containingClass = method.getContainingClass();
                if (ClassUtils.isSubclass(containingClass,
                        "java.util.Hashtable")) {
                    registerMethodCallError(expression, KEY_SET_ITERATOR_TEXT);
                }
            }
        }

        private static boolean isEnumerationMethodCalled(
                @NotNull PsiVariable variable, @NotNull PsiElement context) {
            final EnumerationMethodCalledVisitor visitor =
                    new EnumerationMethodCalledVisitor(variable);
            context.accept(visitor);
            return visitor.isEnumerationMethodCalled();
        }

        private static class EnumerationMethodCalledVisitor
                extends PsiRecursiveElementVisitor {

            private final PsiVariable variable;
            private boolean enumerationMethodCalled = false;

            EnumerationMethodCalledVisitor(@NotNull PsiVariable variable) {
                this.variable = variable;
            }

            public void visitMethodCallExpression(
                    PsiMethodCallExpression expression) {
                if (enumerationMethodCalled) {
                    return;
                }
                super.visitMethodCallExpression(expression);
                final PsiReferenceExpression methodExpression =
                        expression.getMethodExpression();
                final String methodName = methodExpression.getReferenceName();
                if (!"hasMoreElements".equals(methodName) &&
                        !"nextElement".equals(methodName)) {
                    return;
                }
                final PsiExpression qualifierExpression =
                        methodExpression.getQualifierExpression();
                if (!(qualifierExpression instanceof PsiReferenceExpression)) {
                    return;
                }
                final PsiReferenceExpression referenceExpression =
                        (PsiReferenceExpression) qualifierExpression;
                final PsiElement element = referenceExpression.resolve();
                if (!(element instanceof PsiVariable)) {
                    return;
                }
                final PsiVariable variable = (PsiVariable) element;
                enumerationMethodCalled = this.variable.equals(variable);
            }

            public boolean isEnumerationMethodCalled() {
                return enumerationMethodCalled;
            }
        }
    }
}