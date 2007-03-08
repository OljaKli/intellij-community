/*
 * Copyright 2003-2007 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.threading;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import org.jetbrains.annotations.NotNull;

public class VolatileLongOrDoubleFieldInspection extends BaseInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "volatile.long.or.double.field.display.name");
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final PsiType type = (PsiType)infos[0];
        final String typeString = type.getPresentableText();
        return InspectionGadgetsBundle.message(
                "volatile.field.problem.descriptor", typeString);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new VolatileLongOrDoubleFieldVisitor();
    }

    private static class VolatileLongOrDoubleFieldVisitor
            extends BaseInspectionVisitor {

        public void visitField(@NotNull PsiField field) {
            super.visitField(field);
            if (!field.hasModifierProperty(PsiModifier.VOLATILE)) {
                return;
            }
            final PsiType type = field.getType();
            if (PsiType.LONG.equals(type) || PsiType.DOUBLE.equals(type)) {
                registerFieldError(field, type);
            }
        }
    }
}