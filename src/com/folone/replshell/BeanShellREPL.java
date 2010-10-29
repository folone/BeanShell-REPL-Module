package com.folone.replshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import bsh.EvalError;

import com.folone.Evaluator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class BeanShellREPL extends Service {
	
	private Evaluator.Stub evaluator = new Evaluator.Stub() {
		
		public String evaluate(String script) throws RemoteException {
			StringBuffer strResult = new StringBuffer();
		    Object result = null;
		    
		    // out and err are transient in bsh.Interpreter, so we reset them
		    // on each request just to make sure they're there.
		    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		    PrintStream output = new PrintStream(outputBuffer);
		    
		    bsh.Interpreter interpreter;
		    interpreter = new bsh.Interpreter(null, null, null, false);
		    
		    interpreter.setOut(output);
		    interpreter.setErr(output);
		    
			// We set System.out and System.err such that we capture anything
		    // that runs during interpretation (Such as a System.out.println
		    // call). The _only_ reason this doesn't fail due to concurrency,
		    // is because we don't get concurrent requests to a single JVM.
		    PrintStream out = System.out;
		    PrintStream err = System.err;
		    System.setOut(output);
		    System.setErr(output);
		    
		    try {
		      // Eval the user text
		      result = interpreter.eval(script);
		    } catch (EvalError e) {
				e.printStackTrace();
			} finally {
		      System.setOut(out);
		      System.setErr(err);
		    }

		    output.flush();
		    strResult.append(new String(outputBuffer.toByteArray()));
		    
		    if (result != null) {
		      strResult.append(result.toString());
		    }

		    String ret = strResult.toString();
		    final String oddPrefix = "experiment: creating class manager\n";
		    if (ret.startsWith(oddPrefix))
		      ret = ret.substring(oddPrefix.length());
		    return ret;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return evaluator;
	}

}
