import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import javax.swing.JFileChooser;

/**

// WORKED ALONGSIDE ZOE RICHARDS AND KALEB CAMPBELL. CODE MAY APPEAR SIMILAR
 * The parser and interpreter. The top level parse function, a main method for
 * testing, and several utility methods are provided. You need to implement
 * parseProgram and all the rest of the parser.
 */
public class Parser {

	/**
	 * Top level parse method, called by the World
	 */
	static RobotProgramNode parseFile(File code) {
		Scanner scan = null;
		try {
			scan = new Scanner(code);

			// the only time tokens can be next to each other is
			// when one of them is one of (){},;
			scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

			RobotProgramNode n = parseProgram(scan); // You need to implement this!!!

			scan.close();
			return n;
		} catch (FileNotFoundException e) {
			System.out.println("Robot program source file not found");
		} catch (ParserFailureException e) {
			System.out.println("Parser error:");
			System.out.println(e.getMessage());
			scan.close();
		}
		return null;
	}

	/** For testing the parser without requiring the world */

	public static void main(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				File f = new File(arg);
				if (f.exists()) {
					System.out.println("Parsing '" + f + "'");
					RobotProgramNode prog = parseFile(f);
					System.out.println("Parsing completed ");
					if (prog != null) {
						System.out.println("================\nProgram:");
						System.out.println(prog);
					}
					System.out.println("=================");
				} else {
					System.out.println("Can't find file '" + f + "'");
				}
			}
		} else {
			while (true) {
				JFileChooser chooser = new JFileChooser(".");// System.getProperty("user.dir"));
				int res = chooser.showOpenDialog(null);
				if (res != JFileChooser.APPROVE_OPTION) {
					break;
				}
				RobotProgramNode prog = parseFile(chooser.getSelectedFile());
				System.out.println("Parsing completed");
				if (prog != null) {
					System.out.println("Program: \n" + prog);
				}
				System.out.println("=================");
			}
		}
		System.out.println("Done");
	}

	// Useful Patterns

	static Pattern NUMPAT = Pattern.compile("-?(0|[1-9][0-9]*)"); // ("-?(0|[1-9][0-9]*)");
	static Pattern OPENPAREN = Pattern.compile("\\(");
	static Pattern CLOSEPAREN = Pattern.compile("\\)");
	static Pattern OPENBRACE = Pattern.compile("\\{");
	static Pattern CLOSEBRACE = Pattern.compile("\\}");
	static Pattern COMMA = Pattern.compile(","); //pattern for comma
	static Pattern ELSEPAT = Pattern.compile("else"); //pattern for else
	static Pattern OPPAT = Pattern.compile("add|sub|mul|div"); //pattern for add/sub ect. Alltogether

	/**
	 * PROG ::= STMT+
	 */
	
	//parser is here 
	static RobotProgramNode parseProgram(Scanner s) {
		List<StatementNode> statements = new ArrayList<>(); //create new arrayList
		while (s.hasNext()) { //while next, cast 
			StatementNode stNode = (StatementNode) parseStatement(s);
			statements.add(stNode); //add node
		}
		return new ProgramNode(statements); //return statements
	}

	// STMT :: ACT ";" | LOOP
	static RobotProgramNode parseStatement(Scanner s) {
		ActNode act = null;
		LoopNode loop = null;
		WhileNode whileNode = null;
		IfNode ifNode = null;
		if (s.hasNext("loop")) {
			loop = (LoopNode) parseLoop(s);
		} else if (s.hasNext("if")) {
			ifNode = (IfNode) parseIf(s);
		} else if (s.hasNext("while")) {
			whileNode = (WhileNode) parseWhile(s);
		} else {
			// Instead of checking to make sure the action string is valid in this method,
			// pass the responsibility onto the parseAct method
			act = (ActNode) parseAct(s);
			require(";", "Semicolon missing!", s);
		}
		return new StatementNode(act, loop, ifNode, whileNode);
	}

	static RobotProgramNode parseAct(Scanner s) {
		String action = s.next();

		if (s.hasNext(OPENPAREN) && (action.equals("move") || action.equals("wait"))) {
			s.next();
			ExpNode exp = (ExpNode) parseExp(s);
			require(CLOSEPAREN, "Closing parenthesis missing!", s);
			return new ActNode(action, exp);
		}
//movement, if move...
		if (action.equals("move") || action.equals("turnL") || action.equals("turnR") || action.equals("takeFuel")
				|| action.equals("wait") || action.equals("shieldOn") || action.equals("shieldOff")
				|| action.equals("turnAround")) {

		} else { //the string mjst not be valid
			fail("The action string is not valid!", s);
		}

		return new ActNode(action, null);
	}

	static RobotProgramNodeInteger parseSen(Scanner s) { //static varible for parser 
		String sen = s.next();
		if (sen.equals("fuelLeft") || sen.equals("oppLR") || sen.equals("oppFB") || sen.equals("numBarrels")
				|| sen.equals("barrelLR") || sen.equals("barrelFB") || sen.equals("wallDist")) {

		} else {
			fail("The sen string is not valid!", s);
		}

		return new SenNode(sen);
	}



	static RobotProgramNode parseLoop(Scanner s) {

		s.next();
		BlockNode b = (BlockNode) parseBlock(s);
		return new LoopNode(b);

	}

	static RobotProgramNode parseWhile(Scanner s) {

		CondNode condNode = null;
		BlockNode block = null;

		require("while", "Missing while statement", s);
		require(OPENPAREN, "Opening parenthesis missing!", s);
		condNode = (CondNode) parseCond(s); //cast
		require(CLOSEPAREN, "Closing parenthesis missing!", s); 
		//if closing bracket is missing, 
		block = (BlockNode) parseBlock(s);

		return new WhileNode(condNode, block);

	}

	static RobotProgramNode parseIf(Scanner s) {

		CondNode condNode = null;
		BlockNode block = null;
		BlockNode elseBlock = null;

		require("if", "Missing if statement", s);
		require(OPENPAREN, "Opening parenthesis missing!", s);
		condNode = (CondNode) parseCond(s);
		require(CLOSEPAREN, "Closing parenthesis missing!", s);
		block = (BlockNode) parseBlock(s);

		if (s.hasNext(ELSEPAT)) {
			s.next();
			elseBlock = (BlockNode) parseBlock(s);
		}

		return new IfNode(condNode, block, elseBlock);

	}

	static RobotProgramNodeCond parseCond(Scanner s) {


		if (s.hasNext("lt") || s.hasNext("gt") || s.hasNext("eq")) {
			String relop = s.next();
			require(OPENPAREN, "Opening parenthesis missing!", s);
			ExpNode e1 = (ExpNode) parseExp(s);
			require(COMMA, "Missing a comma", s);
			ExpNode e2 = (ExpNode) parseExp(s);
			require(CLOSEPAREN, "Closing parenthesis missing!", s);
			return new CondNode(e1, e2, relop, null, null, null);
		} else if (s.hasNext("and") || s.hasNext("or")) {
			String logOp = s.next();
			require(OPENPAREN, "Opening parenthesis missing!", s);
			CondNode c1 = (CondNode) parseCond(s);
			require(COMMA, "Missing a comma", s);
			CondNode c2 = (CondNode) parseCond(s);
			require(CLOSEPAREN, "Closing parenthesis missing!", s);
			return new CondNode(null, null, null, logOp, c1, c2);
		} else if (s.hasNext("not")) {
			String logOp = s.next();
			require(OPENPAREN, "Opening parenthesis missing!", s);
			CondNode c = (CondNode) parseCond(s);
			require(CLOSEPAREN, "Closing parenthesis missing!", s);
			return new CondNode(null, null, null, logOp, c, null);
		} else {
			fail("Not a suitable condition", s);
			return null;
		}
	}

	static RobotProgramNodeInteger parseExp(Scanner s) {

		int number = 0;
		SenNode sen = null;
		OpNode op = null;

		if (s.hasNext(NUMPAT)) {
			number = s.nextInt();
		} else if (s.hasNext(OPPAT)) {
			op = (OpNode) parseOp(s);
		} else {
			sen = (SenNode) parseSen(s);
		}

		return new ExpNode(op, sen, number);
	}

	static RobotProgramNodeInteger parseOp(Scanner s) {

		String op = s.next();
		require(OPENPAREN, "Opening parenthesis missing!", s);
		ExpNode e1 = (ExpNode) parseExp(s);
		require(COMMA, "Missing comma", s);
		ExpNode e2 = (ExpNode) parseExp(s);
		require(CLOSEPAREN, "Closing parenthesis missing!", s);

		if (op.equals("add") || op.equals("sub") || op.equals("mul") || op.equals("div")) {

		} else {
			fail("Incorrect op", s);
		}

		return new OpNode(op, e1, e2);
	}

	static RobotProgramNode parseBlock(Scanner s) {
		List<StatementNode> statements = new ArrayList<>();
		require(OPENBRACE, "Opening brace missing!", s);
		if (s.hasNext(CLOSEBRACE)) {
			fail("A block node needs to have at least one statement!", s);
		}
		while (s.hasNext()) {
			if (s.hasNext(CLOSEBRACE)) {
				break;
			}
			StatementNode stNode = (StatementNode) parseStatement(s);
			statements.add(stNode);
		}
		require(CLOSEBRACE, "Closing brace missing!", s);
		if (statements.size() < 1) {
			fail("A block node needs to have at least one statement!", s);
		}
		return new BlockNode(statements);
	}

	// utility methods for the parser

	/**
	 * Report a failure in the parser.
	 */
	static void fail(String message, Scanner s) {
		String msg = message + "\n   @ ...";
		for (int i = 0; i < 5 && s.hasNext(); i++) {
			msg += " " + s.next();
		}
		throw new ParserFailureException(msg + "...");
	}

	/**
	 * Requires that the next token matches a pattern if it matches, it consumes and
	 * returns the token, if not, it throws an exception with an error message
	 */
	static String require(String p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	static String require(Pattern p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	/**
	 * Requires that the next token matches a pattern (which should only match a
	 * number) if it matches, it consumes and returns the token as an integer if
	 * not, it throws an exception with an error message
	 */
	static int requireInt(String p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	static int requireInt(Pattern p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	/**
	 * Checks whether the next token in the scanner matches the specified pattern,
	 * if so, consumes the token and return true. Otherwise returns false without
	 * consuming anything.
	 */
	static boolean checkFor(String p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

	static boolean checkFor(Pattern p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

}

// You could add the node classes here, as long as they are not declared public
// (or private)
