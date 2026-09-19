import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

public class L extends MIDlet implements CommandListener {

	private boolean started;
	private long startHeap;
	
	// state
	private StringBuffer sb;
	private boolean jsr;

	protected void destroyApp(boolean unconditional) {}

	protected void pauseApp() {}

	protected void startApp() {
		if (started) return;
		started = true;
		startHeap = Runtime.getRuntime().totalMemory();
		
		init();
	}
	
	void init() {
		Display display = Display.getDisplay(this);
		display.setCurrent(new Form("Loading"));
		
		long m = testMem();
		
		Form form = new Form("j2me-tester2 v" + getAppProperty("MIDlet-Version"));
		form.addCommand(new Command("Exit", Command.EXIT, 0));
//		form.addCommand(new Command("Change view", Command.SCREEN, 1));
		form.setCommandListener(this);
		
		StringBuffer sb = this.sb = new StringBuffer();
		String s = System.getProperty("microedition.platform");
		
		sb.append("microedition.platform: ").append(s);
		
		sb.append("\n\nJava:\n");

		if (s != null && s.indexOf("sw_platform=S60") != -1) {
			int i = s.indexOf("java_build_version=");
			if (i != -1) {
				int l = Math.min(s.indexOf('/', i + 19), s.indexOf(';', i + 19));
				if (l == -1) l = s.length();
				String v = s.substring(i + 19, l);
				if (v.startsWith("2.")) {
					sb.append("Java Runtime ").append(v).append(" for Symbian");
				} else {
					sb.append("Java Runtime ").append(v).append(" for S60");
				}
				if ((s = System.getProperty("com.sonyericsson.java.platform")) != null) {
					sb.append(", ").append(s).append("\n");
				}
				sb.append("\n");
			}
		} else if ((s = System.getProperty("com.sonyericsson.java.platform")) != null) {
			sb.append("Sony Ericsson Java Platform ").append(s).append("\n");
		}
		// TODO detect Java Runtime for S40, Asha
		
		sb.append("MIDP ");
		if (checkClass("javax.microedition.lcdui.TabbedPane")) {
			sb.append("3.0");
		} else if (checkClass("javax.microedition.lcdui.Spacer")) {
			if ((s = System.getProperty("microedition.profiles")) != null && s.indexOf("MIDP-2.1") != -1) {
				sb.append("2.1");
			} else {
				sb.append("2.0");
			}
		} else {
			sb.append("1.0");
		}
		sb.append(", ");
		if (checkClass("java.lang.Enum") || checkClass("java.util.regex.Pattern")) {
			sb.append("JDK");
		} else if (checkClass("java.lang.CharSequence")) {
			sb.append("CDC 1.1");
		} else if (checkClass("java.lang.SecurityManager")) {
			sb.append("CDC 1.0");
		} else if (checkClass("java.security.Permission")) {
			sb.append("CLDC 1.1.1");
		} else if (checkClass("java.lang.Float")) {
			sb.append("CLDC 1.1");
		} else {
			sb.append("CLDC 1.0");
		}
		
		sb.append('\n').append(m != startHeap ? "'''Max heap size''': ": "'''Heap size''': ");
		if (m >= 1024 * 1024 && m % (1024 * 1024) == 0) {
			sb.append(m / 1024 / 1024).append(" MB");
		} else if (m >= 512 * 1024) {
			sb.append(m / 1024).append(" KB");
		} else {
			sb.append(m).append(" bytes");
		}
		
		sb.append("\n\nJava APIs:\n");
		
		if (checkClass("javax.microedition.io.file.FileConnection") || checkClass("javax.microedition.pim.PIM")) {
			appendJsr("75");
			if (!checkClass("javax.microedition.pim.PIM")) {
				appendRemark("FileConnection only");
			} else if (!checkClass("javax.microedition.io.file.FileConnection")) {
				appendRemark("PIM only");
			}
		}
		
		if (checkClass("javax.bluetooth.LocalDevice") || checkClass("javax.obex.HeaderSet")) {
			appendJsr("82");
			if (!checkClass("javax.obex.HeaderSet")) {
				appendRemark("no OBEX");
			}
		}
		
		if (checkClass("javax.wireless.messaging.Message") && !checkClass("javax.wireless.messaging.MessagePart")) {
			appendJsr("120");
		}
		
		if (checkClass("javax.microedition.media.control.RecordControl")
				|| (checkClass("javax.microedition.media.Manager") && !checkClass("javax.microedition.lcdui.Spacer"))) {
			appendJsr("135");
		}
		
		if (checkClass("javax.microedition.xml.rpc.Type") || checkClass("javax.xml.parsers.SAXParser")) {
			appendJsr("172");
		}
		
		if (checkClass("javax.microedition.apdu.APDUConnection") 
				|| checkClass("javax.crypto.Cipher")
				|| checkClass("javax.microedition.jcrmi.JavaCardRMIConnection")
				|| checkClass("javax.microedition.pki.UserCredentialManager")) {
			appendJsr("177");
			int c = 0;
			StringBuffer sb2 = new StringBuffer();
			if (checkClass("javax.microedition.apdu.APDUConnection")) {
				if (c != 0) sb2.append(", ");
				sb2.append("APDU");
				c++;
			}
			if (checkClass("javax.crypto.Cipher")) {
				if (c != 0) sb2.append(", ");
				sb2.append("Crypto");
				c++;
			}
			if (checkClass("javax.microedition.jcrmi.JavaCardRMIConnection")) {
				if (c != 0) sb2.append(", ");
				sb2.append("JCRMI");
				c++;
			}
			if (checkClass("javax.microedition.pki.UserCredentialManager")) {
				if (c != 0) sb2.append(", ");
				sb2.append("PKI");
				c++;
			}
			
			if (c == 1) {
				appendRemark(sb2.append(" only").toString());
			} else if (c != 4) {
				appendRemark(sb2.toString());
			}
		}
		
		if (checkClass("javax.microedition.location.Location")) {
			appendJsr("179");
		}
		
		if (checkClass("javax.microedition.sip.SipConnection")) {
			appendJsr("180");
		}
		
		if (checkClass("javax.microedition.m3g.Node")) {
			appendJsr("184");
		}
		
		if (System.getProperty("microedition.jtwi.version") != null && System.getProperty("microedition.msa.version") == null) {
			appendJsr("185");
		}
		
		if (checkClass("javax.wireless.messaging.MessagePart")) {
			appendJsr("205");
		}
		
		if (checkClass("javax.microedition.content.ContentHandler")) {
			appendJsr("211");
		}
		
		if (checkClass("javax.microedition.m2g.ScalableGraphics")) {
			appendJsr("226");
		}
		
		if (checkClass("javax.microedition.payment.TransactionModule")) {
			appendJsr("229");
		}
		
		if (checkClass("javax.microedition.amms.GlobalManager")) {
			appendJsr("234");
		}
		
		if (checkClass("javax.microedition.global.ResourceManager")) {
			appendJsr("238");
		}
		
		if (checkClass("javax.microedition.khronos.opengles.GL")) {
			appendJsr("239");
		}
		
		if (System.getProperty("microedition.msa.version") != null) {
			appendJsr("248");
		}
		
		if (checkClass("javax.microedition.sensor.SensorManager")) {
			appendJsr("256");
		}
		
		if (checkClass("javax.microedition.contactless.DiscoveryManager")) {
			appendJsr("257");
		}
		
		if (checkClass("javax.microedition.theme.Capabilities")) {
			appendJsr("258");
		}
		
		if (checkClass("javax.microedition.broadcast.connection.BroadcastConnection")
				&& System.getProperty("microedition.broadcast.version") != null) {
			appendJsr("272");
		}
		
		if (checkClass("javax.xml.XMLConstants")) {
			appendJsr("280");
		}
		
		if (checkClass("javax.microedition.drm.DRMManager")) {
			appendJsr("300");
		}
		
		if (checkClass("org.eclipse.ercp.swt.mobile.MobileShell")) {
			appendOem("eSWT API");
		}
		
		if (checkClass("com.mascotcapsule.micro3d.v3.Graphics3D")) {
			appendOem("Mascot Capsule v3");
		}
		
		if ((s = System.getProperty("com.nokia.mid.ui.version")) != null) {
			appendOem("Nokia UI API ".concat(s));
		} else if (checkClass("com.nokia.mid.ui.VirtualKeyboard")) {
			appendOem("Nokia UI API 1.6");
		} else if (checkClass("com.nokia.mid.ui.SoftNotification")) {
			if (System.getProperty("com.nokia.mid.ui.customfontsize") == null) {
				// early s60v3.2
				appendOem("Nokia UI API 1.1");
			} else {
				appendOem("Nokia UI API 1.4");
			}
		} else if (System.getProperty("com.nokia.mid.ui.customfontsize") != null) {
			appendOem("Nokia UI API 1.1c");
		} else if (checkClass("com.nokia.mid.ui.Clipboard")) {
			appendOem("Nokia UI API 1.1b");
		} else if (System.getProperty("com.nokia.mid.ui.joystick_event") != null) {
			appendOem("Nokia UI API 1.1a");
		} else if (checkClass("com.nokia.mid.ui.DeviceControl")
				|| checkClass("com.nokia.mid.ui.DirectGraphics")) {
			appendOem(checkClass("javax.microedition.lcdui.Spacer") ? "Nokia UI API 1.1" : "Nokia UI API 1.0");
		} else if (checkClass("com.nokia.mid.sound.Sound")) {
			appendOem("Nokia Sound API");
		}
		
		if (checkClass("com.nokia.mid.iapinfo.IAPInfo")) {
			appendOem("Nokia IAP Info API");
		}
		
		if (checkClass("com.nokia.payment.NPayManager")) {
			appendOem("Nokia In-App Payment API");
		}
		
		if (checkClass("com.nokia.mid.payment.IAPClientPaymentManager")) {
			appendOem("Nokia In-App Purchase API");
		}
		
		if (checkClass("com.pantech.titan.PantechAudio")) {
			appendOem("Pantech Audio API");
		}
		
		if (checkClass("com.sonyericsson.capuchin.FlashCanvas")) {
			appendOem("Project Capuchin API");
		}
		
		if (checkClass("net.rim.device.api.system.Application")) {
			appendOem("RIM API");
		}
		
		if (checkClass("com.samsung.util.AudioClip")) {
			appendOem("Samsung API");
		}
		
		if (checkClass("com.sprintpcs.media.Player")) {
			appendOem("Sprint Media API");
		}
		
		if (checkClass("com.vodafone.v10.system.device.DeviceControl")) {
			appendOem("Vodafone API 1.0");
		}
		
		if (checkClass("com.vodafone.system.DeviceControl")) {
			appendOem("Vodafone API 2.0");
		}
		
		if (checkClass("com.siemens.mp.NotAllowedException")) {
			appendOem("Siemens API");
		}
		
		if (jsr) {
			sb.append("}}");
			jsr = false;
		}
		form.append(sb.toString());
		display.setCurrent(form);
	}
	
	private void appendJsr(String s) {
		if (!jsr) {
			sb.append("{{JSR|");
		} else {
			sb.append("|");
		}
		sb.append(s);
		jsr = true;
	}
	
	private void appendRemark(String s) {
		if (jsr) {
			sb.append("}}");
			jsr = false;
		}
		sb.append(" {{remark|").append(s).append("}}<br>\n");
	}
	
	private void appendOem(String s) {
		if (jsr) {
			sb.append("}}\n");
			jsr = false;
		}
		sb.append("<br>").append(s).append("\n");
	}

	public void commandAction(Command c, Displayable d) {
		if (c.getCommandType() == Command.EXIT) {
			notifyDestroyed();
			return;
		}
	}
	
	private static long testMem() {
		long m = 0;
		try {
			Vector v = new Vector();
			while (true) {
				v.addElement(new byte[102400]);
				m = Runtime.getRuntime().totalMemory();
			}
		} catch (Throwable e) {
			m = Runtime.getRuntime().totalMemory();
		} finally {
			System.gc();
		}
		return m;
	}
	
	static boolean checkClass(String s) {
		try {
			// pluotsorbet returns null instead of throwing ClassNotFoundException
			return Class.forName(s) != null;
//		} catch (SecurityException e) {
//			return true;
		} catch (Exception e) {
			return false;
		} catch (Error e) {
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
}
