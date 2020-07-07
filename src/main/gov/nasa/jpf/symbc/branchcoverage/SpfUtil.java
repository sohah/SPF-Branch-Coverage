package gov.nasa.jpf.symbc.branchcoverage;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemGeneral;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemZ3BitVectorIncremental;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemZ3Incremental;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;

import java.io.File;


/**
 * This class provides some utility methods for SPF.
 */
public class SpfUtil {

    private static int operandNum;

    /**
     * Returns number of operands depending on the if-bytecode instruction.
     *
     * @param instruction"if" bytecode instruction
     * @return Number of operands.
     *
     */
    public static int getOperandNumber(String instruction) {
        switch (instruction) {
            case "ifeq":
            case "ifne":
            case "iflt":
            case "ifle":
            case "ifgt":
            case "ifge":
            case "ifnull":
            case "ifnonnull":
                operandNum = 1;
                break;
            case "if_icmpeq":
            case "if_icmpne":
            case "if_icmpgt":
            case "if_icmpge":
            case "if_icmple":
            case "if_icmplt":
            case "if_acmpne":
                operandNum = 2;
                break;
            default:
                operandNum = -1;
        }
        return operandNum;
    }

    /**
     * Checks if the "if" condition is symbolic based on the the operands of the "if" bytecode instruction.
     *
     * @param ti  Current ThreadInfo object
     * @param ins Current "if" bytecode instruction.
     * @return True if the operand(s) of "if" condition is symbolic and false if it was concerete.
     *
     */
    public static boolean isSymCond(ThreadInfo ti, Instruction ins) {
        StackFrame sf = ti.getTopFrame();
        boolean isSymCondition = false;
        SpfUtil.getOperandNumber(ins.getMnemonic());
        gov.nasa.jpf.symbc.numeric.Expression operand1, operand2;
        if (operandNum == 1) {
            operand1 = (gov.nasa.jpf.symbc.numeric.Expression)
                    sf.getOperandAttr();
            if (operand1 != null)
                isSymCondition = true;
            /*if (isSymCondition && VeritestingListener.performanceMode) {
                if (operand1 instanceof IntegerExpression) operand2 = new IntegerConstant(0);
                else if (operand1 instanceof RealExpression) operand2 = new RealConstant(0.0);
                else
                    return false; // we cannot figure this condition out
                isSymCondition = isBothSidesFeasible(ti, getComparator(ins), getNegComparator(ins), operand1,
                        operand2);
            }*/
        }
        if (operandNum == 2) {
            operand1 = (gov.nasa.jpf.symbc.numeric.Expression)
                    sf.getOperandAttr(1);
            if (operand1 != null)
                isSymCondition = true;
            operand2 = (gov.nasa.jpf.symbc.numeric.Expression)
                    sf.getOperandAttr(0);
            if (operand2 != null)
                isSymCondition = true;
            /*if (isSymCondition && VeritestingListener.performanceMode) {
                if (operand1 == null) {
                    if (operand2 instanceof IntegerExpression) operand1 = new IntegerConstant(sf.peek(1));
                    else if (operand2 instanceof RealExpression) operand1 = new RealConstant(sf.peekDouble(1));
                    else
                        return false; // we cannot figure this condition out
                } else if (operand2 == null) {
                    if (operand1 instanceof IntegerExpression) operand2 = new IntegerConstant(sf.peek(0));
                    else if (operand1 instanceof RealExpression) operand2 = new RealConstant(sf.peekDouble(0));
                    else
                        return false; // we cannot figure this condition out
                }
                isSymCondition = isBothSidesFeasible(ti, getComparator(ins), getNegComparator(ins), operand1, operand2);
            }*/
        }
        if (operandNum == -11)
            isSymCondition = false;

        return isSymCondition;
    }



    public static Comparator getComparator(Instruction instruction) {
        switch (instruction.getMnemonic()) {
            case "ifeq":
            case "if_icmpeq":
                return Comparator.EQ;
            case "ifge":
            case "if_icmpge":
                return Comparator.GE;
            case "ifle":
            case "if_icmple":
                return Comparator.LE;
            case "ifgt":
            case "if_icmpgt":
                return Comparator.GT;
            case "iflt":
            case "if_icmplt":
                return Comparator.LT;
            case "ifne":
            case "if_icmpne":
                return Comparator.NE;
            default:
                System.out.println("Unknown comparator: " + instruction.getMnemonic());
                assert (false);
                return null;
        }
    }

    public static Comparator getNegComparator(Instruction instruction) {
        switch (instruction.getMnemonic()) {
            case "ifeq":
            case "if_icmpeq":
                return Comparator.NE;
            case "ifge":
            case "if_icmpge":
                return Comparator.LT;
            case "ifle":
            case "if_icmple":
                return Comparator.GT;
            case "ifgt":
            case "if_icmpgt":
                return Comparator.LE;
            case "iflt":
            case "if_icmplt":
                return Comparator.GE;
            case "ifne":
            case "if_icmpne":
                return Comparator.EQ;
            default:
                System.out.println("Unknown comparator: " + instruction.getMnemonic());
                assert (false);
                return null;
        }
    }
}
