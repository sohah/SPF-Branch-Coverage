/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package gov.nasa.jpf.symbc.bytecode.branchchoices.util;


import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;


public class IFInstrSymbHelper {
    public static boolean flipBranchExploration = false;

    public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti,
                                                               IfInstruction instr,
                                                               gov.nasa.jpf.symbc.numeric.IntegerExpression sym_v1,
                                                               gov.nasa.jpf.symbc.numeric.IntegerExpression sym_v2,
                                                               gov.nasa.jpf.symbc.numeric.Comparator trueComparator,
                                                               gov.nasa.jpf.symbc.numeric.Comparator falseComparator) {

        //TODO: fix conditionValue
        if (!ti.isFirstStepInsn()) { // first time around
            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator prevPcGen;
            ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
            if (cg instanceof gov.nasa.jpf.symbc.numeric.PCChoiceGenerator)
                prevPcGen = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) cg;
            else
                prevPcGen = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) cg.getPreviousChoiceGeneratorOfType(gov.nasa.jpf.symbc.numeric.PCChoiceGenerator.class);

            gov.nasa.jpf.symbc.numeric.PathCondition pc;
            if (prevPcGen != null)
                pc = prevPcGen.getCurrentPC();
            else
                pc = new gov.nasa.jpf.symbc.numeric.PathCondition();

            gov.nasa.jpf.symbc.numeric.PathCondition eqPC = pc.make_copy();
            gov.nasa.jpf.symbc.numeric.PathCondition nePC = pc.make_copy();

            int v2 = ti.getModifiableTopFrame().peek();
            int v1 = ti.getModifiableTopFrame().peek(1);

            if (sym_v1 != null) {
                if (sym_v2 != null) { //both are symbolic values
                    eqPC._addDet(trueComparator, sym_v1, sym_v2);
                    nePC._addDet(falseComparator, sym_v1, sym_v2);
                } else {
                    eqPC._addDet(trueComparator, sym_v1, v2);
                    nePC._addDet(falseComparator, sym_v1, v2);
                }
            } else {
                eqPC._addDet(trueComparator, v1, sym_v2);
                nePC._addDet(falseComparator, v1, sym_v2);
            }

            boolean eqSat = eqPC.simplify();
            boolean neSat = nePC.simplify();

            if (eqSat) {
                if (neSat) {
                    BranchChoiceGenerator newPCChoice = new BranchChoiceGenerator(2, flipBranchExploration);
                    newPCChoice.setOffset(instr.getPosition());
                    newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
                    ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
                    return instr;
                } else {
                    ti.getModifiableTopFrame().pop();
                    ti.getModifiableTopFrame().pop();
                    return instr.getTarget();
                }
            } else {
                ti.getModifiableTopFrame().pop();
                ti.getModifiableTopFrame().pop();
                return instr.getNext(ti);
            }
        } else { //This branch will only be taken if there is a choice

            int v2 = ti.getModifiableTopFrame().pop();
            int v1 = ti.getModifiableTopFrame().pop();
            gov.nasa.jpf.symbc.numeric.PathCondition pc;
            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator curCg = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) ti.getVM().getSystemState().getChoiceGenerator();

            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(gov.nasa.jpf.symbc.numeric.PCChoiceGenerator.class);

            if (prevCg == null)
                pc = new gov.nasa.jpf.symbc.numeric.PathCondition();
            else
                pc = prevCg.getCurrentPC();

            boolean conditionValue = (Integer) curCg.getNextChoice() == 1 ? true : false;

            conditionValue = ((BranchChoiceGenerator) curCg).flip ? !conditionValue : conditionValue;

            if (conditionValue) {
                if (sym_v1 != null) {
                    if (sym_v2 != null) { //both are symbolic values
                        pc._addDet(trueComparator, sym_v1, sym_v2);
                    } else
                        pc._addDet(trueComparator, sym_v1, v2);
                } else
                    pc._addDet(trueComparator, v1, sym_v2);
                ((gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) curCg).setCurrentPC(pc);
                return instr.getTarget();
            } else {
                if (sym_v1 != null) {
                    if (sym_v2 != null) { //both are symbolic values
                        pc._addDet(falseComparator, sym_v1, sym_v2);
                    } else
                        pc._addDet(falseComparator, sym_v1, v2);
                } else
                    pc._addDet(falseComparator, v1, sym_v2);
                ((gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) curCg).setCurrentPC(pc);
                return instr.getNext(ti);
            }
        }
    }



    public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti,
                                                               IfInstruction instr,
                                                               gov.nasa.jpf.symbc.numeric.IntegerExpression sym_v,
                                                               gov.nasa.jpf.symbc.numeric.Comparator trueComparator,
                                                               gov.nasa.jpf.symbc.numeric.Comparator falseComparator) {

        //TODO: fix conditionValue
        if(!ti.isFirstStepInsn()) { // first time around
            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator prevPcGen;
            ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
            if(cg instanceof gov.nasa.jpf.symbc.numeric.PCChoiceGenerator)
                prevPcGen = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator)cg;
            else
                prevPcGen = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(gov.nasa.jpf.symbc.numeric.PCChoiceGenerator.class);

            gov.nasa.jpf.symbc.numeric.PathCondition pc;
            if(prevPcGen!=null)
                pc = prevPcGen.getCurrentPC();
            else
                pc = new gov.nasa.jpf.symbc.numeric.PathCondition();

            gov.nasa.jpf.symbc.numeric.PathCondition eqPC = pc.make_copy();
            gov.nasa.jpf.symbc.numeric.PathCondition nePC = pc.make_copy();
            eqPC._addDet(trueComparator, sym_v, 0);
            nePC._addDet(falseComparator, sym_v, 0);

            boolean eqSat = eqPC.simplify();
            boolean neSat = nePC.simplify();

            if(eqSat) {
                if(neSat) {
                    BranchChoiceGenerator newPCChoice = new BranchChoiceGenerator(2, flipBranchExploration);
                    newPCChoice.setOffset(instr.getPosition());
                    newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
                    ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
                    return instr;
                } else {
                    ti.getModifiableTopFrame().pop();
                    return instr.getTarget();
                }
            } else {
                ti.getModifiableTopFrame().pop();
                return instr.getNext(ti);
            }
        } else {
            ti.getModifiableTopFrame().pop();
            gov.nasa.jpf.symbc.numeric.PathCondition pc;
            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator curCg = (gov.nasa.jpf.symbc.numeric.PCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();

            gov.nasa.jpf.symbc.numeric.PCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(gov.nasa.jpf.symbc.numeric.PCChoiceGenerator.class);

            if(prevCg == null )
                pc = new gov.nasa.jpf.symbc.numeric.PathCondition();
            else
                pc = prevCg.getCurrentPC();
            boolean conditionValue = (Integer)curCg.getNextChoice()==1 ? true: false;

            conditionValue = ((BranchChoiceGenerator) curCg).flip ? !conditionValue : conditionValue;

            if(conditionValue) {
                pc._addDet(trueComparator, sym_v, 0);
                ((gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) curCg).setCurrentPC(pc);
                return instr.getTarget();
            } else {
                pc._addDet(falseComparator, sym_v, 0);
                ((gov.nasa.jpf.symbc.numeric.PCChoiceGenerator) curCg).setCurrentPC(pc);
                return instr.getNext(ti);
            }
        }
    }

}
