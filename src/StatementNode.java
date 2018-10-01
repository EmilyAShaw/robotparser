import java.util.ArrayList;
import java.util.List;


// WORKED ALONGSIDE ZOE RICHARDS AND KALEB CAMPBELL. CODE MAY APPEAR SIMILAR


//i,mplements programnode interface
public class StatementNode implements RobotProgramNode {

	ActNode actNode;
	LoopNode loopNode;
	IfNode ifNode;
	WhileNode whileNode;
//constructor
	public StatementNode(ActNode actNode, LoopNode loopNode, IfNode ifNode, WhileNode whileNode) {
		this.actNode = actNode;
		this.loopNode = loopNode;
		this.ifNode = ifNode;
		this.whileNode = whileNode;
	}

	@Override
	
	//execute method, executes nodes.
	public void execute(Robot robot) {
		if (actNode != null) {
			actNode.execute(robot);
		} else if (ifNode != null) {
			ifNode.execute(robot);
		} else if (whileNode != null) {
			whileNode.execute(robot);
		} else {
			loopNode.execute(robot);
		}
	}

}

//i,mplements programnode interface

class ProgramNode implements RobotProgramNode {

	List<StatementNode> statements = new ArrayList<>();

	public ProgramNode(List<StatementNode> statements) {
		this.statements = statements; //list of statments
	}

	@Override
	public void execute(Robot robot) {
		for (StatementNode statement : statements) { //cycling through statements
			statement.execute(robot); 
		}
	}

}

class ActNode implements RobotProgramNode { //actnode class, implements interface roboto program node 

	String action;
	ExpNode exp;

	public ActNode(String action, ExpNode exp) {
		this.action = action;
		this.exp = exp;
	}

	@Override
	public void execute(Robot robot) {
		if (action.equals("move")) {
			if (exp != null) {
				for (int i = 0; i < exp.execute(robot); i++) {
					robot.move();
				}
			} else {
				robot.move();
			}
		} else if (action.equals("turnL")) {
			robot.turnLeft();
		} else if (action.equals("turnR")) {
			robot.turnRight();
		} else if (action.equals("takeFuel")) {
			robot.takeFuel();
		} else if (action.equals("wait")) {
			if (exp != null) {
				for (int i = 0; i < exp.execute(robot); i++) {
					robot.idleWait();
				}
			} else {
				robot.idleWait();
			}
		} else if (action.equals("shieldOn")) {
			robot.setShield(true);
		} else if (action.equals("shieldOff")) {
			robot.setShield(false);
		} else if (action.equals("turnAround")) {
			robot.turnAround();
		}
	}

}

//sennode implements robotprogramnode integer

class SenNode implements RobotProgramNodeInteger {

	String sen; //sensor

	public SenNode(String sen) {
		this.sen = sen;
	}

	@Override
	public int execute(Robot robot) {
		if (sen.equals("fuelLeft")) {
			return robot.getFuel();
		} else if (sen.equals("oppLR")) {
			return robot.getOpponentLR();
		} else if (sen.equals("oppFB")) {
			return robot.getOpponentFB();
		} else if (sen.equals("numBarrels")) {
			return robot.numBarrels();
		} else if (sen.equals("barrelLR")) {
			return robot.getClosestBarrelLR();
		} else if (sen.equals("barrelFB")) {
			return robot.getClosestBarrelFB();
		} else if (sen.equals("wallDist")) {
			return robot.getDistanceToWall();
		}

		return 0;
	}

}

//loopnode imlements node interface
class LoopNode implements RobotProgramNode {

	BlockNode blocknode;

	public LoopNode(BlockNode blocknode) {
		this.blocknode = blocknode;
	}

	@Override
	public void execute(Robot robot) {
		blocknode.execute(robot);
	}

}
//condition node implements nodecond interface
class CondNode implements RobotProgramNodeCond {

	ExpNode e1;
	ExpNode e2;
	String op;
	String logOp;
	CondNode c1;
	CondNode c2;

	public CondNode(ExpNode e1, ExpNode e2, String op, String logOp, CondNode c1, CondNode c2) {
		this.e1 = e1;
		this.e2 = e2;
		this.op = op;
		this.logOp = logOp;
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public boolean execute(Robot robot) {
		if (op != null) {
			if (op.equals("lt")) {
				return e1.execute(robot) < e2.execute(robot);
			} else if (op.equals("gt")) {
				return e1.execute(robot) > e2.execute(robot);
			} else if (op.equals("eq")) {
				return e1.execute(robot) == e2.execute(robot);
			}
		} else {
			if (logOp.equals("and")) {
				return c1.execute(robot) && c2.execute(robot);
			} else if (logOp.equals("or")) {
				return c1.execute(robot) || c2.execute(robot);
			} else if (logOp.equals("not")) {
				return !c1.execute(robot);
			}
		}

		return false;
	}

}

class IfNode implements RobotProgramNode {
//if implements node interface
	CondNode cond;
	BlockNode block;
	BlockNode eBlock;

	public IfNode(CondNode cond, BlockNode block, BlockNode eBlock) {
		this.cond = cond;
		this.block = block;
		this.eBlock = eBlock;
	}

	@Override
	public void execute(Robot robot) {
		if (cond.execute(robot)) {
			block.execute(robot);
		} else {
			if (eBlock != null) {
				eBlock.execute(robot);
			}
		}
	}

}

class WhileNode implements RobotProgramNode {
//loop/while implements node
	CondNode cond;
	BlockNode block;

	public WhileNode(CondNode cond, BlockNode block) {
		this.cond = cond;
		this.block = block;
	}

	@Override
	public void execute(Robot robot) {
		while (cond.execute(robot)) {
			block.execute(robot);
		}
	}

}

class BlockNode implements RobotProgramNode {

	List<StatementNode> statements = new ArrayList<>(); //arraylist of statements

	public BlockNode(List<StatementNode> statements) {
		this.statements = statements;
	}

	@Override
	public void execute(Robot robot) {
		for (StatementNode statement : statements) {
			statement.execute(robot);
		}
	}

}

//implements integer
class ExpNode implements RobotProgramNodeInteger {

	OpNode opNode;
	SenNode senNode;
	int number;

	public ExpNode(OpNode opNode, SenNode senNode, int number) {
		this.opNode = opNode;
		this.senNode = senNode;
		this.number = number;
	}

	@Override
	public int execute(Robot robot) {
		if (opNode != null) {
			return opNode.execute(robot);
		} else if (senNode != null) {
			return senNode.execute(robot);
		} else {
			return number;
		}
	}

}

class OpNode implements RobotProgramNodeInteger {

	String op;
	ExpNode e1;
	ExpNode e2;

	public OpNode(String op, ExpNode e1, ExpNode e2) {
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	public int execute(Robot robot) {
		if (op.equals("add")) {
			return e1.execute(robot) + e2.execute(robot);
		} else if (op.equals("sub")) {
			return e1.execute(robot) - e2.execute(robot);
		} else if (op.equals("mul")) {
			return e1.execute(robot) * e2.execute(robot);
		} else if (op.equals("div")) {
			return e1.execute(robot) / e2.execute(robot);
		}

		return 0;
	}

}