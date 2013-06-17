package com.yttrium.scrotter;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	private static final boolean fullScreen = false;
	private static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "com.yttrium.scrotter", "com.yttrium.scrotter.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                anywheresoftware.b4a.keywords.Common.Log("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "com.yttrium.scrotter", "com.yttrium.scrotter.main");
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        initializeProcessGlobals();		
        initializeGlobals();
        
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true)
				return true;
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public static class _panelinfo{
public boolean IsInitialized;
public int PanelType;
public boolean LayoutLoaded;
public void Initialize() {
IsInitialized = true;
PanelType = 0;
LayoutLoaded = false;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public anywheresoftware.b4a.keywords.Common __c = null;
public static int _type_about = 0;
public static int _type_preview = 0;
public static int _type_options = 0;
public static int _fill_parent = 0;
public static int _wrap_content = 0;
public static int _currentpage = 0;
public static String _version = "";
public static String _releasedate = "";
public static String _theme = "";
public static boolean[] _loaded = null;
public static anywheresoftware.b4a.objects.preferenceactivity.PreferenceScreenWrapper _prefscreen = null;
public static anywheresoftware.b4a.objects.preferenceactivity.PreferenceManager _prefmanager = null;
public anywheresoftware.b4a.objects.PanelWrapper _aboutpage = null;
public anywheresoftware.b4a.objects.PanelWrapper _optionspage = null;
public anywheresoftware.b4a.objects.PanelWrapper _previewpage = null;
public de.amberhome.viewpager.AHPageContainer _container = null;
public de.amberhome.viewpager.AHViewPager _pager = null;
public de.amberhome.viewpager.AHViewPagerTabs _tabs = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _glosscheckbox = null;
public anywheresoftware.b4a.objects.SpinnerWrapper _modelbox = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _shadowcheckbox = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _stretchcheckbox = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _undershadowcheckbox = null;
public anywheresoftware.b4a.objects.SpinnerWrapper _variantbox = null;
public anywheresoftware.b4a.objects.TabHostWrapper _tabswitcher = null;
public anywheresoftware.b4a.objects.ProgressBarWrapper _loading = null;
public anywheresoftware.b4a.objects.ButtonWrapper _loadbtn = null;
public anywheresoftware.b4a.objects.ButtonWrapper _savebtn = null;
public anywheresoftware.b4a.objects.PanelWrapper _preview = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _loadedimage = null;
public anywheresoftware.b4a.agraham.threading.Threading _backgroundthread = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _previewimage = null;
public static boolean _waiting = false;
public anywheresoftware.b4a.phone.Phone.ContentChooser _cc = null;
public anywheresoftware.b4a.phone.RingtoneManagerWrapper _ringtone = null;
public anywheresoftware.b4a.objects.LabelWrapper _scrottertitle = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _iconview = null;
public anywheresoftware.b4a.objects.LabelWrapper _scrottervers = null;
public anywheresoftware.b4a.objects.ButtonWrapper _themebtn = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _finalbitmap = null;
public static String _savedphone = "";
public static String _savedvariant = "";
public static String _savephone = "";
public anywheresoftware.b4a.objects.collections.List _themelist = null;
public anywheresoftware.b4a.objects.collections.List _devicelist = null;
public anywheresoftware.b4a.keywords.constants.TypefaceWrapper _ubunturegular = null;
public anywheresoftware.b4a.keywords.constants.TypefaceWrapper _ubuntulight = null;
public anywheresoftware.b4a.objects.ButtonWrapper _settingsbtn = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _settingsicon = null;
public com.yttrium.scrotter.statemanager _statemanager = null;
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 83;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 85;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 86;BA.debugLine="CreatePreferenceScreen";
_createpreferencescreen();
 //BA.debugLineNum = 87;BA.debugLine="If PrefManager.GetAll.Size = 0 Then SetDefaults";
if (_prefmanager.GetAll().getSize()==0) { 
_setdefaults();};
 };
 //BA.debugLineNum = 89;BA.debugLine="theme = StateManager.GetSetting2(\"theme\", \"Dark\")";
_theme = mostCurrent._statemanager._getsetting2(mostCurrent.activityBA,"theme","Dark");
 //BA.debugLineNum = 90;BA.debugLine="savedphone = StateManager.GetSetting2(\"savedphone\", \"\")";
mostCurrent._savedphone = mostCurrent._statemanager._getsetting2(mostCurrent.activityBA,"savedphone","");
 //BA.debugLineNum = 91;BA.debugLine="savedvariant = StateManager.GetSetting2(\"savedvariant\", \"\")";
mostCurrent._savedvariant = mostCurrent._statemanager._getsetting2(mostCurrent.activityBA,"savedvariant","");
 //BA.debugLineNum = 92;BA.debugLine="savephone = StateManager.GetSetting2(\"savephone\", \"False\")";
mostCurrent._savephone = mostCurrent._statemanager._getsetting2(mostCurrent.activityBA,"savephone","False");
 //BA.debugLineNum = 94;BA.debugLine="container.Initialize";
mostCurrent._container.Initialize(mostCurrent.activityBA);
 //BA.debugLineNum = 95;BA.debugLine="aboutpage = CreatePanel(TYPE_ABOUT, \"About\")";
mostCurrent._aboutpage = _createpanel(_type_about,"About");
 //BA.debugLineNum = 96;BA.debugLine="container.AddPage(aboutpage, \"About\")";
mostCurrent._container.AddPage((android.view.View)(mostCurrent._aboutpage.getObject()),"About");
 //BA.debugLineNum = 97;BA.debugLine="previewpage = CreatePanel(TYPE_PREVIEW, \"Preview\")";
mostCurrent._previewpage = _createpanel(_type_preview,"Preview");
 //BA.debugLineNum = 98;BA.debugLine="container.AddPage(previewpage,\"Preview\")";
mostCurrent._container.AddPage((android.view.View)(mostCurrent._previewpage.getObject()),"Preview");
 //BA.debugLineNum = 99;BA.debugLine="optionspage = CreatePanel(TYPE_OPTIONS, \"Options\")";
mostCurrent._optionspage = _createpanel(_type_options,"Options");
 //BA.debugLineNum = 100;BA.debugLine="container.AddPage(optionspage,\"Options\")";
mostCurrent._container.AddPage((android.view.View)(mostCurrent._optionspage.getObject()),"Options");
 //BA.debugLineNum = 101;BA.debugLine="pager.Initialize(container, \"Pager\")";
mostCurrent._pager.Initialize(mostCurrent.activityBA,mostCurrent._container,"Pager");
 //BA.debugLineNum = 102;BA.debugLine="tabs.Initialize(pager)";
mostCurrent._tabs.Initialize(mostCurrent.activityBA,mostCurrent._pager);
 //BA.debugLineNum = 103;BA.debugLine="tabs.LineHeight = 5dip";
mostCurrent._tabs.setLineHeight(anywheresoftware.b4a.keywords.Common.DipToCurrent((int)(5)));
 //BA.debugLineNum = 104;BA.debugLine="tabs.UpperCaseTitle = True";
mostCurrent._tabs.setUpperCaseTitle(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 105;BA.debugLine="Activity.AddView(tabs, 0, 0, FILL_PARENT, WRAP_CONTENT)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._tabs.getObject()),(int)(0),(int)(0),_fill_parent,_wrap_content);
 //BA.debugLineNum = 106;BA.debugLine="Activity.AddView(pager, 0, 29dip, Activity.Width, Activity.Height-29dip)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._pager.getObject()),(int)(0),anywheresoftware.b4a.keywords.Common.DipToCurrent((int)(29)),mostCurrent._activity.getWidth(),(int)(mostCurrent._activity.getHeight()-anywheresoftware.b4a.keywords.Common.DipToCurrent((int)(29))));
 //BA.debugLineNum = 107;BA.debugLine="BackgroundThread.Initialise(\"ImageThread\")";
mostCurrent._backgroundthread.Initialise(processBA,"ImageThread");
 //BA.debugLineNum = 108;BA.debugLine="cc.Initialize(\"cc\")";
mostCurrent._cc.Initialize("cc");
 //BA.debugLineNum = 109;BA.debugLine="Select theme";
switch (BA.switchObjectToInt(_theme,"Light","Dark")) {
case 0:
 //BA.debugLineNum = 111;BA.debugLine="tabs.Color = Colors.White";
mostCurrent._tabs.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 112;BA.debugLine="tabs.BackgroundColorPressed = Colors.DarkGray";
mostCurrent._tabs.setBackgroundColorPressed(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 113;BA.debugLine="tabs.LineColorCenter = Colors.DarkGray";
mostCurrent._tabs.setLineColorCenter(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 114;BA.debugLine="tabs.TextColor = Colors.LightGray";
mostCurrent._tabs.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 115;BA.debugLine="tabs.TextColorCenter = Colors.DarkGray";
mostCurrent._tabs.setTextColorCenter(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 116;BA.debugLine="tabs.Invalidate";
mostCurrent._tabs.Invalidate();
 break;
case 1:
 //BA.debugLineNum = 118;BA.debugLine="tabs.Color = Colors.RGB(50, 50, 50)";
mostCurrent._tabs.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 119;BA.debugLine="tabs.BackgroundColorPressed = Colors.White";
mostCurrent._tabs.setBackgroundColorPressed(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 120;BA.debugLine="tabs.LineColorCenter = Colors.LightGray";
mostCurrent._tabs.setLineColorCenter(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 121;BA.debugLine="tabs.TextColor = Colors.Gray";
mostCurrent._tabs.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 122;BA.debugLine="tabs.TextColorCenter = Colors.LightGray";
mostCurrent._tabs.setTextColorCenter(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 123;BA.debugLine="tabs.Invalidate";
mostCurrent._tabs.Invalidate();
 break;
}
;
 //BA.debugLineNum = 125;BA.debugLine="End Sub";
return "";
}
public static boolean  _activity_keypress(int _keycode) throws Exception{
 //BA.debugLineNum = 714;BA.debugLine="Sub activity_KeyPress (KeyCode As Int) As Boolean";
 //BA.debugLineNum = 715;BA.debugLine="If KeyCode = KeyCodes.KEYCODE_BACK Then";
if (_keycode==anywheresoftware.b4a.keywords.Common.KeyCodes.KEYCODE_BACK) { 
 //BA.debugLineNum = 716;BA.debugLine="If (pager.CurrentPage = 1) = False AND pager.PagingEnabled = True Then";
if ((mostCurrent._pager.getCurrentPage()==1)==anywheresoftware.b4a.keywords.Common.False && mostCurrent._pager.getPagingEnabled()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 717;BA.debugLine="pager.GotoPage(1, True)";
mostCurrent._pager.GotoPage((int)(1),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 718;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 //BA.debugLineNum = 721;BA.debugLine="End Sub";
return false;
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 272;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 273;BA.debugLine="CurrentPage = pager.CurrentPage";
_currentpage = mostCurrent._pager.getCurrentPage();
 //BA.debugLineNum = 274;BA.debugLine="StateManager.SaveSettings";
mostCurrent._statemanager._savesettings(mostCurrent.activityBA);
 //BA.debugLineNum = 275;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
anywheresoftware.b4a.objects.IntentWrapper _in = null;
String _uristring = "";
int _x = 0;
 //BA.debugLineNum = 228;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 229;BA.debugLine="pager.GotoPage(CurrentPage, False)";
mostCurrent._pager.GotoPage(_currentpage,anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 230;BA.debugLine="Activity.RerunDesignerScript(\"About\", pager.Width, pager.Height)";
mostCurrent._activity.RerunDesignerScript("About",mostCurrent.activityBA,mostCurrent._pager.getWidth(),mostCurrent._pager.getHeight());
 //BA.debugLineNum = 231;BA.debugLine="Activity.RerunDesignerScript(\"Preview\", pager.Width, pager.Height)";
mostCurrent._activity.RerunDesignerScript("Preview",mostCurrent.activityBA,mostCurrent._pager.getWidth(),mostCurrent._pager.getHeight());
 //BA.debugLineNum = 232;BA.debugLine="Activity.RerunDesignerScript(\"Options\", pager.Width, pager.Height)";
mostCurrent._activity.RerunDesignerScript("Options",mostCurrent.activityBA,mostCurrent._pager.getWidth(),mostCurrent._pager.getHeight());
 //BA.debugLineNum = 233;BA.debugLine="Dim In As Intent";
_in = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 234;BA.debugLine="In = Activity.GetStartingIntent";
_in = mostCurrent._activity.GetStartingIntent();
 //BA.debugLineNum = 235;BA.debugLine="If In.ExtrasToString.Contains(\"no extras\") Then";
if (_in.ExtrasToString().contains("no extras")) { 
 }else {
 //BA.debugLineNum = 238;BA.debugLine="Log(In.ExtrasToString)";
anywheresoftware.b4a.keywords.Common.Log(_in.ExtrasToString());
 //BA.debugLineNum = 239;BA.debugLine="Dim UriString As String";
_uristring = "";
 //BA.debugLineNum = 240;BA.debugLine="UriString = In.ExtrasToString";
_uristring = _in.ExtrasToString();
 //BA.debugLineNum = 241;BA.debugLine="UriString = UriString.SubString2(UriString.IndexOf(\"STREAM=\")+7,UriString.IndexOf(\"}\"))";
_uristring = _uristring.substring((int)(_uristring.indexOf("STREAM=")+7),_uristring.indexOf("}"));
 //BA.debugLineNum = 242;BA.debugLine="If UriString.Contains(\",\") Then";
if (_uristring.contains(",")) { 
 //BA.debugLineNum = 243;BA.debugLine="UriString = UriString.SubString2(0,UriString.IndexOf(\",\"))";
_uristring = _uristring.substring((int)(0),_uristring.indexOf(","));
 };
 //BA.debugLineNum = 245;BA.debugLine="Log(UriString)";
anywheresoftware.b4a.keywords.Common.Log(_uristring);
 //BA.debugLineNum = 246;BA.debugLine="LoadedImage.Initialize3(LoadBitmap(Ringtone.GetContentDir, UriString))";
mostCurrent._loadedimage.Initialize3((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(mostCurrent._ringtone.GetContentDir(),_uristring).getObject()));
 //BA.debugLineNum = 247;BA.debugLine="Preview.SetBackgroundImage(ResizeImage(LoadedImage, Preview.Width, Preview.Height))";
mostCurrent._preview.SetBackgroundImage((android.graphics.Bitmap)(_resizeimage(mostCurrent._loadedimage,mostCurrent._preview.getWidth(),mostCurrent._preview.getHeight()).getObject()));
 //BA.debugLineNum = 248;BA.debugLine="pager.GotoPage(1, False)";
mostCurrent._pager.GotoPage((int)(1),anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 250;BA.debugLine="ScrotterTitle.Typeface = UbuntuRegular";
mostCurrent._scrottertitle.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 251;BA.debugLine="ScrotterVers.Typeface = UbuntuRegular";
mostCurrent._scrottervers.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 252;BA.debugLine="Loadbtn.Typeface = UbuntuRegular";
mostCurrent._loadbtn.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 253;BA.debugLine="SaveBtn.Typeface = UbuntuRegular";
mostCurrent._savebtn.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 254;BA.debugLine="GlossCheckbox.Typeface = UbuntuRegular";
mostCurrent._glosscheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 255;BA.debugLine="ShadowCheckbox.Typeface = UbuntuRegular";
mostCurrent._shadowcheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 256;BA.debugLine="UnderShadowCheckbox.Typeface = UbuntuRegular";
mostCurrent._undershadowcheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 257;BA.debugLine="theme = PrefManager.GetString(\"theme\")";
_theme = _prefmanager.GetString("theme");
 //BA.debugLineNum = 258;BA.debugLine="StateManager.SetSetting(\"theme\", theme)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"theme",_theme);
 //BA.debugLineNum = 259;BA.debugLine="RefreshTheme";
_refreshtheme();
 //BA.debugLineNum = 260;BA.debugLine="If PrefManager.GetBoolean(\"retaindevice\") = True Then savephone = \"True\" Else savephone = \"False\"";
if (_prefmanager.GetBoolean("retaindevice")==anywheresoftware.b4a.keywords.Common.True) { 
mostCurrent._savephone = "True";}
else {
mostCurrent._savephone = "False";};
 //BA.debugLineNum = 261;BA.debugLine="StateManager.SetSetting(\"savephone\", savephone)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"savephone",mostCurrent._savephone);
 //BA.debugLineNum = 262;BA.debugLine="If savephone = \"True\" Then";
if ((mostCurrent._savephone).equals("True")) { 
 //BA.debugLineNum = 263;BA.debugLine="For x = 0 To ModelBox.Size - 1";
{
final double step228 = 1;
final double limit228 = (int)(mostCurrent._modelbox.getSize()-1);
for (_x = (int)(0); (step228 > 0 && _x <= limit228) || (step228 < 0 && _x >= limit228); _x += step228) {
 //BA.debugLineNum = 264;BA.debugLine="If ModelBox.GetItem(x) = savedphone Then ModelBox.SelectedIndex = x";
if ((mostCurrent._modelbox.GetItem(_x)).equals(mostCurrent._savedphone)) { 
mostCurrent._modelbox.setSelectedIndex(_x);};
 }
};
 };
 //BA.debugLineNum = 267;BA.debugLine="StateManager.SetSetting(\"savedphone\", savedphone)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"savedphone",mostCurrent._savedphone);
 //BA.debugLineNum = 268;BA.debugLine="StateManager.SetSetting(\"savedvariant\", savedvariant)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"savedvariant",mostCurrent._savedvariant);
 //BA.debugLineNum = 269;BA.debugLine="ModelBox_itemClick(0, \"\")";
_modelbox_itemclick((int)(0),(Object)(""));
 //BA.debugLineNum = 270;BA.debugLine="End Sub";
return "";
}
public static String  _cc_result(boolean _success,String _dir,String _filename) throws Exception{
 //BA.debugLineNum = 221;BA.debugLine="Sub CC_Result (Success As Boolean, Dir As String, FileName As String)";
 //BA.debugLineNum = 222;BA.debugLine="If Success = True Then";
if (_success==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 223;BA.debugLine="LoadedImage.Initialize3(LoadBitmap(Ringtone.GetContentDir, FileName))";
mostCurrent._loadedimage.Initialize3((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(mostCurrent._ringtone.GetContentDir(),_filename).getObject()));
 //BA.debugLineNum = 224;BA.debugLine="ImageProcess";
_imageprocess();
 };
 //BA.debugLineNum = 226;BA.debugLine="End Sub";
return "";
}
public static anywheresoftware.b4a.objects.PanelWrapper  _createpanel(int _paneltype,String _title) throws Exception{
anywheresoftware.b4a.objects.PanelWrapper _pan = null;
com.yttrium.scrotter.main._panelinfo _pi = null;
 //BA.debugLineNum = 277;BA.debugLine="Sub CreatePanel(PanelType As Int, Title As String) As Panel";
 //BA.debugLineNum = 278;BA.debugLine="Dim pan As Panel";
_pan = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 279;BA.debugLine="Dim pi As PanelInfo";
_pi = new com.yttrium.scrotter.main._panelinfo();
 //BA.debugLineNum = 280;BA.debugLine="pi.Initialize";
_pi.Initialize();
 //BA.debugLineNum = 281;BA.debugLine="pi.LayoutLoaded = False";
_pi.LayoutLoaded = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 282;BA.debugLine="pi.PanelType = PanelType";
_pi.PanelType = _paneltype;
 //BA.debugLineNum = 283;BA.debugLine="pan.Initialize(\"\")";
_pan.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 284;BA.debugLine="pan.Tag = pi";
_pan.setTag((Object)(_pi));
 //BA.debugLineNum = 285;BA.debugLine="Return pan";
if (true) return _pan;
 //BA.debugLineNum = 286;BA.debugLine="End Sub";
return null;
}
public static String  _createpreferencescreen() throws Exception{
anywheresoftware.b4a.objects.preferenceactivity.PreferenceCategoryWrapper _cat1 = null;
anywheresoftware.b4a.objects.preferenceactivity.PreferenceCategoryWrapper _cat2 = null;
anywheresoftware.b4a.objects.IntentWrapper _intent1 = null;
anywheresoftware.b4a.objects.IntentWrapper _intent2 = null;
 //BA.debugLineNum = 735;BA.debugLine="Sub CreatePreferenceScreen";
 //BA.debugLineNum = 736;BA.debugLine="PrefScreen.Initialize(\"Scrotter\", \"\")";
_prefscreen.Initialize("Scrotter","");
 //BA.debugLineNum = 738;BA.debugLine="Dim cat1, cat2 As AHPreferenceCategory";
_cat1 = new anywheresoftware.b4a.objects.preferenceactivity.PreferenceCategoryWrapper();
_cat2 = new anywheresoftware.b4a.objects.preferenceactivity.PreferenceCategoryWrapper();
 //BA.debugLineNum = 739;BA.debugLine="cat1.Initialize(\"Settings\")";
_cat1.Initialize("Settings");
 //BA.debugLineNum = 740;BA.debugLine="cat1.AddCheckBox(\"retaindevice\", \"Save Device\", \"Save current device as default\", \"Don't save current device as default\", ParseSavePhone, \"\")";
_cat1.AddCheckBox("retaindevice","Save Device","Save current device as default","Don't save current device as default",_parsesavephone(),"");
 //BA.debugLineNum = 741;BA.debugLine="cat1.AddList(\"theme\", \"Theme\", \"\", \"Dark\", Null, themelist)";
_cat1.AddList("theme","Theme","","Dark",String.valueOf(anywheresoftware.b4a.keywords.Common.Null),mostCurrent._themelist);
 //BA.debugLineNum = 742;BA.debugLine="cat2.Initialize(\"About\")";
_cat2.Initialize("About");
 //BA.debugLineNum = 743;BA.debugLine="Dim Intent1, Intent2 As Intent";
_intent1 = new anywheresoftware.b4a.objects.IntentWrapper();
_intent2 = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 744;BA.debugLine="Intent1.Initialize(Intent1.ACTION_VIEW, \"https://play.google.com/store/apps/details?id=com.yttrium.scrotter\")";
_intent1.Initialize(_intent1.ACTION_VIEW,"https://play.google.com/store/apps/details?id=com.yttrium.scrotter");
 //BA.debugLineNum = 745;BA.debugLine="Intent2.Initialize(Intent2.ACTION_VIEW, \"mailto:cab@gmail.com?subject=[Scrotter4Android] YourIssueHere&body=Please write your feedback here.\")";
_intent2.Initialize(_intent2.ACTION_VIEW,"mailto:cab@gmail.com?subject=[Scrotter4Android] YourIssueHere&body=Please write your feedback here.");
 //BA.debugLineNum = 746;BA.debugLine="cat2.AddIntent(\"Check for updates\", \"v\" & version & \" (\" & releasedate & \")\", Intent1, \"\")";
_cat2.AddIntent("Check for updates","v"+_version+" ("+_releasedate+")",(android.content.Intent)(_intent1.getObject()),"");
 //BA.debugLineNum = 747;BA.debugLine="cat2.AddIntent(\"Contact Us\", \"via email\", Intent2, \"\")";
_cat2.AddIntent("Contact Us","via email",(android.content.Intent)(_intent2.getObject()),"");
 //BA.debugLineNum = 749;BA.debugLine="PrefScreen.AddPreferenceCategory(cat1)";
_prefscreen.AddPreferenceCategory(_cat1);
 //BA.debugLineNum = 750;BA.debugLine="PrefScreen.AddPreferenceCategory(cat2)";
_prefscreen.AddPreferenceCategory(_cat2);
 //BA.debugLineNum = 751;BA.debugLine="End Sub";
return "";
}
public static String  _endloading() throws Exception{
 //BA.debugLineNum = 659;BA.debugLine="Sub EndLoading";
 //BA.debugLineNum = 660;BA.debugLine="Loading.Visible = False";
mostCurrent._loading.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 661;BA.debugLine="Preview.SetBackgroundImage(PreviewImage)";
mostCurrent._preview.SetBackgroundImage((android.graphics.Bitmap)(mostCurrent._previewimage.getObject()));
 //BA.debugLineNum = 662;BA.debugLine="pager.PagingEnabled = True";
mostCurrent._pager.setPagingEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 663;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (processGlobalsRun == false) {
	    processGlobalsRun = true;
		try {
		        main._process_globals();
statemanager._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 32;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 35;BA.debugLine="Dim aboutpage, optionspage, previewpage As Panel";
mostCurrent._aboutpage = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._optionspage = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._previewpage = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Dim container As AHPageContainer";
mostCurrent._container = new de.amberhome.viewpager.AHPageContainer();
 //BA.debugLineNum = 37;BA.debugLine="Dim pager As AHViewPager";
mostCurrent._pager = new de.amberhome.viewpager.AHViewPager();
 //BA.debugLineNum = 38;BA.debugLine="Dim tabs As AHViewPagerTabs";
mostCurrent._tabs = new de.amberhome.viewpager.AHViewPagerTabs();
 //BA.debugLineNum = 39;BA.debugLine="Dim GlossCheckbox As CheckBox";
mostCurrent._glosscheckbox = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Dim ModelBox As Spinner";
mostCurrent._modelbox = new anywheresoftware.b4a.objects.SpinnerWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Dim ShadowCheckbox As CheckBox";
mostCurrent._shadowcheckbox = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Dim StretchCheckbox As CheckBox";
mostCurrent._stretchcheckbox = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Dim UnderShadowCheckbox As CheckBox";
mostCurrent._undershadowcheckbox = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 44;BA.debugLine="Dim VariantBox As Spinner";
mostCurrent._variantbox = new anywheresoftware.b4a.objects.SpinnerWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Dim TabSwitcher As TabHost";
mostCurrent._tabswitcher = new anywheresoftware.b4a.objects.TabHostWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Dim Loading As ProgressBar";
mostCurrent._loading = new anywheresoftware.b4a.objects.ProgressBarWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Dim Loadbtn As Button";
mostCurrent._loadbtn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Dim SaveBtn As Button";
mostCurrent._savebtn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 49;BA.debugLine="Dim Preview As Panel";
mostCurrent._preview = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Dim LoadedImage As Bitmap";
mostCurrent._loadedimage = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 62;BA.debugLine="Dim BackgroundThread As Thread";
mostCurrent._backgroundthread = new anywheresoftware.b4a.agraham.threading.Threading();
 //BA.debugLineNum = 63;BA.debugLine="Dim PreviewImage As Bitmap";
mostCurrent._previewimage = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 64;BA.debugLine="Dim Waiting As Boolean = False";
_waiting = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 65;BA.debugLine="Dim cc As ContentChooser";
mostCurrent._cc = new anywheresoftware.b4a.phone.Phone.ContentChooser();
 //BA.debugLineNum = 66;BA.debugLine="Dim Ringtone As RingtoneManager";
mostCurrent._ringtone = new anywheresoftware.b4a.phone.RingtoneManagerWrapper();
 //BA.debugLineNum = 67;BA.debugLine="Dim ScrotterTitle As Label";
mostCurrent._scrottertitle = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 68;BA.debugLine="Dim IconView As ImageView";
mostCurrent._iconview = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 69;BA.debugLine="Dim ScrotterVers As Label";
mostCurrent._scrottervers = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 70;BA.debugLine="Dim themebtn As Button";
mostCurrent._themebtn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 71;BA.debugLine="Dim FinalBitmap As Bitmap";
mostCurrent._finalbitmap = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 72;BA.debugLine="Dim savedphone, savedvariant As String";
mostCurrent._savedphone = "";
mostCurrent._savedvariant = "";
 //BA.debugLineNum = 73;BA.debugLine="Dim savephone As String = \"False\"";
mostCurrent._savephone = "False";
 //BA.debugLineNum = 74;BA.debugLine="Dim themelist As List = Array As String(\"Dark\", \"Light\")";
mostCurrent._themelist = new anywheresoftware.b4a.objects.collections.List();
mostCurrent._themelist = anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Dark","Light"});
 //BA.debugLineNum = 75;BA.debugLine="Dim devicelist As List = Array As String(\"Google Nexus 4\", \"Google Nexus 7\", \"Google Nexus S\", \"HTC Desire HD, HTC Inspire 4G\", \"HTC One\", \"HTC One S\", \"HTC One V\", \"HTC One X, HTC One X+\", \"Motorola Droid RAZR\", \"Motorola Droid RAZR M\", \"Motorola Xoom\", \"Samsung Galaxy Note II\", \"Samsung Galaxy Player 5.0\", \"Samsung Galaxy SII, Epic 4G Touch\", \"Samsung Galaxy SIII\", \"Samsung Galaxy SIII Mini\", \"Samsung Google Galaxy Nexus\")";
mostCurrent._devicelist = new anywheresoftware.b4a.objects.collections.List();
mostCurrent._devicelist = anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Google Nexus 4","Google Nexus 7","Google Nexus S","HTC Desire HD, HTC Inspire 4G","HTC One","HTC One S","HTC One V","HTC One X, HTC One X+","Motorola Droid RAZR","Motorola Droid RAZR M","Motorola Xoom","Samsung Galaxy Note II","Samsung Galaxy Player 5.0","Samsung Galaxy SII, Epic 4G Touch","Samsung Galaxy SIII","Samsung Galaxy SIII Mini","Samsung Google Galaxy Nexus"});
 //BA.debugLineNum = 76;BA.debugLine="Dim UbuntuRegular As Typeface = Typeface.LoadFromAssets(\"ubuntureg.ttf\")";
mostCurrent._ubunturegular = new anywheresoftware.b4a.keywords.constants.TypefaceWrapper();
mostCurrent._ubunturegular.setObject((android.graphics.Typeface)(anywheresoftware.b4a.keywords.Common.Typeface.LoadFromAssets("ubuntureg.ttf")));
 //BA.debugLineNum = 77;BA.debugLine="Dim UbuntuLight As Typeface = Typeface.LoadFromAssets(\"ubuntulight.ttf\")";
mostCurrent._ubuntulight = new anywheresoftware.b4a.keywords.constants.TypefaceWrapper();
mostCurrent._ubuntulight.setObject((android.graphics.Typeface)(anywheresoftware.b4a.keywords.Common.Typeface.LoadFromAssets("ubuntulight.ttf")));
 //BA.debugLineNum = 79;BA.debugLine="Dim SettingsBtn As Button";
mostCurrent._settingsbtn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 80;BA.debugLine="Dim SettingsIcon As ImageView";
mostCurrent._settingsicon = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 81;BA.debugLine="End Sub";
return "";
}
public static String  _glosscheckbox_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 723;BA.debugLine="Sub GlossCheckbox_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 724;BA.debugLine="RefreshImage";
_refreshimage();
 //BA.debugLineNum = 725;BA.debugLine="End Sub";
return "";
}
public static String  _imageprocess() throws Exception{
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _device = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper _workingcanvas = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _workingbitmap = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _gloss = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _shadow = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _undershadow = null;
int _indexh = 0;
int _indexw = 0;
com.AB.ABExtDrawing.ABExtDrawing _extdraw = null;
com.AB.ABExtDrawing.ABExtDrawing.ABPaint _paint = null;
String _r480800 = "";
String _r540960 = "";
String _r7201280 = "";
String _r7681280 = "";
String _r8001280 = "";
String _r1280800 = "";
String _r10801920 = "";
anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper _r = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper _r2 = null;
 //BA.debugLineNum = 467;BA.debugLine="Sub ImageProcess";
 //BA.debugLineNum = 468;BA.debugLine="Dim Device As Bitmap";
_device = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 469;BA.debugLine="Dim WorkingCanvas As Canvas";
_workingcanvas = new anywheresoftware.b4a.objects.drawable.CanvasWrapper();
 //BA.debugLineNum = 470;BA.debugLine="Dim WorkingBitmap As Bitmap";
_workingbitmap = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 471;BA.debugLine="Dim Gloss As Bitmap";
_gloss = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 472;BA.debugLine="Dim Shadow As Bitmap";
_shadow = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 473;BA.debugLine="Dim Undershadow As Bitmap";
_undershadow = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 474;BA.debugLine="Dim IndexH As Int";
_indexh = 0;
 //BA.debugLineNum = 475;BA.debugLine="Dim IndexW As Int";
_indexw = 0;
 //BA.debugLineNum = 476;BA.debugLine="Dim ExtDraw As ABExtDrawing";
_extdraw = new com.AB.ABExtDrawing.ABExtDrawing();
 //BA.debugLineNum = 477;BA.debugLine="Dim Paint As ABPaint";
_paint = new com.AB.ABExtDrawing.ABExtDrawing.ABPaint();
 //BA.debugLineNum = 478;BA.debugLine="Dim r480800 As String = \"480x800.png\"";
_r480800 = "480x800.png";
 //BA.debugLineNum = 479;BA.debugLine="Dim r540960 As String = \"540x960.png\"";
_r540960 = "540x960.png";
 //BA.debugLineNum = 480;BA.debugLine="Dim r7201280 As String = \"720x1280.png\"";
_r7201280 = "720x1280.png";
 //BA.debugLineNum = 481;BA.debugLine="Dim r7681280 As String = \"768x1280.png\"";
_r7681280 = "768x1280.png";
 //BA.debugLineNum = 482;BA.debugLine="Dim r8001280 As String = \"800x1280.png\"";
_r8001280 = "800x1280.png";
 //BA.debugLineNum = 483;BA.debugLine="Dim r1280800 As String = \"1280x800.png\"";
_r1280800 = "1280x800.png";
 //BA.debugLineNum = 484;BA.debugLine="Dim r10801920 As String = \"1080x1920.png\"";
_r10801920 = "1080x1920.png";
 //BA.debugLineNum = 485;BA.debugLine="Select Case ModelBox.SelectedItem";
switch (BA.switchObjectToInt(mostCurrent._modelbox.getSelectedItem(),"Samsung Galaxy SIII Mini","HTC Desire HD, HTC Inspire 4G","HTC One X, HTC One X+","Samsung Galaxy SIII","Motorola Xoom","Samsung Galaxy SII, Epic 4G Touch","Samsung Google Galaxy Nexus","Samsung Galaxy Note II","Motorola Droid RAZR","Google Nexus 7","HTC One S","HTC One V","Google Nexus S","Google Nexus 4","Motorola Droid RAZR M","Samsung Galaxy Player 5.0","HTC One")) {
case 0:
 //BA.debugLineNum = 487;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"samsunggsiiimini.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"samsunggsiiimini.png");
 //BA.debugLineNum = 488;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 //BA.debugLineNum = 489;BA.debugLine="IndexW = 78";
_indexw = (int)(78);
 //BA.debugLineNum = 490;BA.debugLine="IndexH = 182";
_indexh = (int)(182);
 break;
case 1:
 //BA.debugLineNum = 492;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"desirehd.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"desirehd.png");
 //BA.debugLineNum = 493;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 //BA.debugLineNum = 494;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"desirehd.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"desirehd.png");
 //BA.debugLineNum = 495;BA.debugLine="IndexW = 86";
_indexw = (int)(86);
 //BA.debugLineNum = 496;BA.debugLine="IndexH = 130";
_indexh = (int)(130);
 break;
case 2:
 //BA.debugLineNum = 498;BA.debugLine="If VariantBox.SelectedItem = \"Black\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Black")) { 
 //BA.debugLineNum = 499;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"onexblack.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"onexblack.png");
 //BA.debugLineNum = 500;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"onexblack.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"onexblack.png");
 //BA.debugLineNum = 501;BA.debugLine="IndexW = 113";
_indexw = (int)(113);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("White") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 503;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"onexwhite.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"onexwhite.png");
 //BA.debugLineNum = 504;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"onexwhite.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"onexwhite.png");
 //BA.debugLineNum = 505;BA.debugLine="IndexW = 115";
_indexw = (int)(115);
 };
 //BA.debugLineNum = 507;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r7201280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r7201280);
 //BA.debugLineNum = 508;BA.debugLine="IndexH = 213";
_indexh = (int)(213);
 break;
case 3:
 //BA.debugLineNum = 510;BA.debugLine="IndexW = 88";
_indexw = (int)(88);
 //BA.debugLineNum = 511;BA.debugLine="If VariantBox.SelectedItem = \"Blue\" OR VariantBox.SelectedItem = \"Pick a variant\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Blue") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 512;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiiiblue.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiiiblue.png");
 //BA.debugLineNum = 513;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"gsiiiblue.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"gsiiiblue.png");
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("White")) { 
 //BA.debugLineNum = 515;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiiiwhite.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiiiwhite.png");
 //BA.debugLineNum = 516;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"gsiiiwhite.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"gsiiiwhite.png");
 //BA.debugLineNum = 517;BA.debugLine="IndexW = 84";
_indexw = (int)(84);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Black")) { 
 //BA.debugLineNum = 519;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiiiblack.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiiiblack.png");
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Red")) { 
 //BA.debugLineNum = 521;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiiired.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiiired.png");
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Brown")) { 
 //BA.debugLineNum = 523;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiiibrown.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiiibrown.png");
 };
 //BA.debugLineNum = 525;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"gsiii.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"gsiii.png");
 //BA.debugLineNum = 526;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r7201280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r7201280);
 //BA.debugLineNum = 527;BA.debugLine="IndexH = 184";
_indexh = (int)(184);
 break;
case 4:
 //BA.debugLineNum = 529;BA.debugLine="If VariantBox.SelectedItem = \"Portrait\" OR VariantBox.SelectedItem = \"Pick a variant\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Portrait") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 530;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"xoomport.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"xoomport.png");
 //BA.debugLineNum = 531;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r8001280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r8001280);
 //BA.debugLineNum = 532;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"xoomport.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"xoomport.png");
 //BA.debugLineNum = 533;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"xoomport.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"xoomport.png");
 //BA.debugLineNum = 534;BA.debugLine="IndexW = 199";
_indexw = (int)(199);
 //BA.debugLineNum = 535;BA.debugLine="IndexH = 200";
_indexh = (int)(200);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Landscape")) { 
 //BA.debugLineNum = 537;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"xoomland.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"xoomland.png");
 //BA.debugLineNum = 538;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r1280800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r1280800);
 //BA.debugLineNum = 539;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"xoomland.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"xoomland.png");
 //BA.debugLineNum = 540;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"xoomland.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"xoomland.png");
 //BA.debugLineNum = 541;BA.debugLine="IndexW = 218";
_indexw = (int)(218);
 //BA.debugLineNum = 542;BA.debugLine="IndexH = 191";
_indexh = (int)(191);
 };
 break;
case 5:
 //BA.debugLineNum = 545;BA.debugLine="If VariantBox.SelectedItem = \"Galaxy SII\" OR VariantBox.SelectedItem = \"Pick a variant\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Galaxy SII") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 546;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsii.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsii.png");
 //BA.debugLineNum = 547;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"gsii.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"gsii.png");
 //BA.debugLineNum = 548;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"gsii.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"gsii.png");
 //BA.debugLineNum = 549;BA.debugLine="IndexW = 132";
_indexw = (int)(132);
 //BA.debugLineNum = 550;BA.debugLine="IndexH = 191";
_indexh = (int)(191);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Epic 4G Touch")) { 
 //BA.debugLineNum = 552;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"epic4gtouch.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"epic4gtouch.png");
 //BA.debugLineNum = 553;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"epic4gtouch.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"epic4gtouch.png");
 //BA.debugLineNum = 554;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"epic4gtouch.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"epic4gtouch.png");
 //BA.debugLineNum = 555;BA.debugLine="IndexW = 132";
_indexw = (int)(132);
 //BA.debugLineNum = 556;BA.debugLine="IndexH = 175";
_indexh = (int)(175);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Galaxy SII (T-Mobile)")) { 
 //BA.debugLineNum = 558;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"gsiitmo.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"gsiitmo.png");
 //BA.debugLineNum = 559;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"gsiitmo.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"gsiitmo.png");
 //BA.debugLineNum = 560;BA.debugLine="IndexW = 61";
_indexw = (int)(61);
 //BA.debugLineNum = 561;BA.debugLine="IndexH = 145";
_indexh = (int)(145);
 };
 //BA.debugLineNum = 563;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 break;
case 6:
 //BA.debugLineNum = 565;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"galaxynexus.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"galaxynexus.png");
 //BA.debugLineNum = 566;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r7201280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r7201280);
 //BA.debugLineNum = 567;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"galaxynexus.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"galaxynexus.png");
 //BA.debugLineNum = 568;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"galaxynexus.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"galaxynexus.png");
 //BA.debugLineNum = 569;BA.debugLine="IndexW = 155";
_indexw = (int)(155);
 //BA.debugLineNum = 570;BA.debugLine="IndexH = 263";
_indexh = (int)(263);
 break;
case 7:
 //BA.debugLineNum = 572;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"galaxynoteii.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"galaxynoteii.png");
 //BA.debugLineNum = 573;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r7201280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r7201280);
 //BA.debugLineNum = 574;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"galaxynoteii.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"galaxynoteii.png");
 //BA.debugLineNum = 575;BA.debugLine="IndexW = 49";
_indexw = (int)(49);
 //BA.debugLineNum = 576;BA.debugLine="IndexH = 140";
_indexh = (int)(140);
 break;
case 8:
 //BA.debugLineNum = 578;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"droidrazr.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"droidrazr.png");
 //BA.debugLineNum = 579;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r540960)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r540960);
 //BA.debugLineNum = 580;BA.debugLine="IndexW = 150";
_indexw = (int)(150);
 //BA.debugLineNum = 581;BA.debugLine="IndexH = 206";
_indexh = (int)(206);
 break;
case 9:
 //BA.debugLineNum = 583;BA.debugLine="If VariantBox.SelectedItem = \"Portrait\" OR VariantBox.SelectedItem = \"Pick a variant\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Portrait") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 584;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"nexus7port.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"nexus7port.png");
 //BA.debugLineNum = 585;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r8001280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r8001280);
 //BA.debugLineNum = 586;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"nexus7port.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"nexus7port.png");
 //BA.debugLineNum = 587;BA.debugLine="Undershadow.Initialize(File.DirAssets, \"undershadow/\" & \"nexus7port.png\")";
_undershadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"undershadow/"+"nexus7port.png");
 //BA.debugLineNum = 588;BA.debugLine="IndexW = 264";
_indexw = (int)(264);
 //BA.debugLineNum = 589;BA.debugLine="IndexH = 311";
_indexh = (int)(311);
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("Landscape")) { 
 //BA.debugLineNum = 591;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"nexus7land.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"nexus7land.png");
 //BA.debugLineNum = 592;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r1280800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r1280800);
 //BA.debugLineNum = 593;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"nexus7land.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"nexus7land.png");
 //BA.debugLineNum = 594;BA.debugLine="IndexW = 315";
_indexw = (int)(315);
 //BA.debugLineNum = 595;BA.debugLine="IndexH = 270";
_indexh = (int)(270);
 };
 break;
case 10:
 //BA.debugLineNum = 598;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"ones.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"ones.png");
 //BA.debugLineNum = 599;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r540960)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r540960);
 //BA.debugLineNum = 600;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"ones.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"ones.png");
 //BA.debugLineNum = 601;BA.debugLine="IndexW = 106";
_indexw = (int)(106);
 //BA.debugLineNum = 602;BA.debugLine="IndexH = 228";
_indexh = (int)(228);
 break;
case 11:
 //BA.debugLineNum = 604;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"onev.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"onev.png");
 //BA.debugLineNum = 605;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 //BA.debugLineNum = 606;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"onev.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"onev.png");
 //BA.debugLineNum = 607;BA.debugLine="IndexW = 85";
_indexw = (int)(85);
 //BA.debugLineNum = 608;BA.debugLine="IndexH = 165";
_indexh = (int)(165);
 break;
case 12:
 //BA.debugLineNum = 610;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"nexuss.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"nexuss.png");
 //BA.debugLineNum = 611;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 //BA.debugLineNum = 612;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"nexuss.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"nexuss.png");
 //BA.debugLineNum = 613;BA.debugLine="IndexW = 45";
_indexw = (int)(45);
 //BA.debugLineNum = 614;BA.debugLine="IndexH = 165";
_indexh = (int)(165);
 break;
case 13:
 //BA.debugLineNum = 616;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"nexus4.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"nexus4.png");
 //BA.debugLineNum = 617;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r7681280)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r7681280);
 //BA.debugLineNum = 618;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"nexus4.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"nexus4.png");
 //BA.debugLineNum = 619;BA.debugLine="IndexW = 45";
_indexw = (int)(45);
 //BA.debugLineNum = 620;BA.debugLine="IndexH = 193";
_indexh = (int)(193);
 break;
case 14:
 //BA.debugLineNum = 622;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"droidrazrm.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"droidrazrm.png");
 //BA.debugLineNum = 623;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r540960)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r540960);
 //BA.debugLineNum = 624;BA.debugLine="IndexW = 49";
_indexw = (int)(49);
 //BA.debugLineNum = 625;BA.debugLine="IndexH = 129";
_indexh = (int)(129);
 break;
case 15:
 //BA.debugLineNum = 627;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"galaxyplay5.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"galaxyplay5.png");
 //BA.debugLineNum = 628;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r480800)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r480800);
 //BA.debugLineNum = 629;BA.debugLine="IndexW = 59";
_indexw = (int)(59);
 //BA.debugLineNum = 630;BA.debugLine="IndexH = 122";
_indexh = (int)(122);
 break;
case 16:
 //BA.debugLineNum = 632;BA.debugLine="If VariantBox.SelectedItem = \"Black\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Black")) { 
 //BA.debugLineNum = 633;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"oneblack.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"oneblack.png");
 }else if((mostCurrent._variantbox.getSelectedItem()).equals("White") || (mostCurrent._variantbox.getSelectedItem()).equals("Pick a variant")) { 
 //BA.debugLineNum = 635;BA.debugLine="Device.Initialize(File.DirAssets, \"device/\" & \"onewhite.png\")";
_device.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"device/"+"onewhite.png");
 };
 //BA.debugLineNum = 637;BA.debugLine="Gloss.Initialize(File.DirAssets, \"gloss/\" & \"one.png\")";
_gloss.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"gloss/"+"one.png");
 //BA.debugLineNum = 638;BA.debugLine="Shadow.Initialize(File.DirAssets, \"shadow/\" & r10801920)";
_shadow.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"shadow/"+_r10801920);
 //BA.debugLineNum = 639;BA.debugLine="IndexW = 160";
_indexw = (int)(160);
 //BA.debugLineNum = 640;BA.debugLine="IndexH = 281";
_indexh = (int)(281);
 break;
}
;
 //BA.debugLineNum = 642;BA.debugLine="Dim R As Rect";
_r = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper();
 //BA.debugLineNum = 643;BA.debugLine="R.Initialize(0, 0, Device.Width, Device.Height)";
_r.Initialize((int)(0),(int)(0),_device.getWidth(),_device.getHeight());
 //BA.debugLineNum = 644;BA.debugLine="WorkingBitmap.InitializeMutable(Device.Width, Device.Height)";
_workingbitmap.InitializeMutable(_device.getWidth(),_device.getHeight());
 //BA.debugLineNum = 645;BA.debugLine="WorkingCanvas.Initialize2(WorkingBitmap)";
_workingcanvas.Initialize2((android.graphics.Bitmap)(_workingbitmap.getObject()));
 //BA.debugLineNum = 646;BA.debugLine="Paint.Initialize()";
_paint.Initialize();
 //BA.debugLineNum = 647;BA.debugLine="If UnderShadowCheckbox.Checked = True AND UnderShadowCheckbox.Enabled = True Then ExtDraw.drawBitmap(WorkingCanvas, Undershadow, Null, R, Paint)";
if (mostCurrent._undershadowcheckbox.getChecked()==anywheresoftware.b4a.keywords.Common.True && mostCurrent._undershadowcheckbox.getEnabled()==anywheresoftware.b4a.keywords.Common.True) { 
_extdraw.drawBitmap(_workingcanvas,(android.graphics.Bitmap)(_undershadow.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r.getObject()),_paint);};
 //BA.debugLineNum = 648;BA.debugLine="Dim r2 As Rect";
_r2 = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper();
 //BA.debugLineNum = 649;BA.debugLine="r2.Initialize(IndexW, IndexH, IndexW + Shadow.Width, IndexH + Shadow.Height)";
_r2.Initialize(_indexw,_indexh,(int)(_indexw+_shadow.getWidth()),(int)(_indexh+_shadow.getHeight()));
 //BA.debugLineNum = 650;BA.debugLine="If LoadedImage.IsInitialized Then ExtDraw.drawBitmap(WorkingCanvas, LoadedImage, Null, r2, Paint)";
if (mostCurrent._loadedimage.IsInitialized()) { 
_extdraw.drawBitmap(_workingcanvas,(android.graphics.Bitmap)(mostCurrent._loadedimage.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r2.getObject()),_paint);};
 //BA.debugLineNum = 651;BA.debugLine="If ShadowCheckbox.Checked = True AND ShadowCheckbox.Enabled = True Then ExtDraw.drawBitmap(WorkingCanvas, Shadow, Null, r2, Paint)";
if (mostCurrent._shadowcheckbox.getChecked()==anywheresoftware.b4a.keywords.Common.True && mostCurrent._shadowcheckbox.getEnabled()==anywheresoftware.b4a.keywords.Common.True) { 
_extdraw.drawBitmap(_workingcanvas,(android.graphics.Bitmap)(_shadow.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r2.getObject()),_paint);};
 //BA.debugLineNum = 652;BA.debugLine="If Device.IsInitialized Then ExtDraw.drawBitmap(WorkingCanvas, Device, Null, R, Paint)";
if (_device.IsInitialized()) { 
_extdraw.drawBitmap(_workingcanvas,(android.graphics.Bitmap)(_device.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r.getObject()),_paint);};
 //BA.debugLineNum = 653;BA.debugLine="If GlossCheckbox.Checked = True AND GlossCheckbox.Enabled = True Then ExtDraw.drawBitmap(WorkingCanvas, Gloss, Null, R, Paint)";
if (mostCurrent._glosscheckbox.getChecked()==anywheresoftware.b4a.keywords.Common.True && mostCurrent._glosscheckbox.getEnabled()==anywheresoftware.b4a.keywords.Common.True) { 
_extdraw.drawBitmap(_workingcanvas,(android.graphics.Bitmap)(_gloss.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r.getObject()),_paint);};
 //BA.debugLineNum = 654;BA.debugLine="PreviewImage.Initialize3(ResizeImage(WorkingBitmap, Preview.Width, Preview.Height))";
mostCurrent._previewimage.Initialize3((android.graphics.Bitmap)(_resizeimage(_workingbitmap,mostCurrent._preview.getWidth(),mostCurrent._preview.getHeight()).getObject()));
 //BA.debugLineNum = 655;BA.debugLine="FinalBitmap.Initialize3(WorkingBitmap)";
mostCurrent._finalbitmap.Initialize3((android.graphics.Bitmap)(_workingbitmap.getObject()));
 //BA.debugLineNum = 656;BA.debugLine="BackgroundThread.RunOnGuiThread(\"EndLoading\", Null)";
mostCurrent._backgroundthread.RunOnGuiThread("EndLoading",(Object[])(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 657;BA.debugLine="End Sub";
return "";
}
public static String  _loadbtn_click() throws Exception{
 //BA.debugLineNum = 460;BA.debugLine="Sub Loadbtn_Click";
 //BA.debugLineNum = 461;BA.debugLine="Try";
try { //BA.debugLineNum = 462;BA.debugLine="cc.Show(\"image/*\", \"\")";
mostCurrent._cc.Show(processBA,"image/*","");
 } 
       catch (Exception e395) {
			processBA.setLastException(e395); };
 //BA.debugLineNum = 465;BA.debugLine="End Sub";
return "";
}
public static String  _modelbox_itemclick(int _position,Object _value) throws Exception{
int _x = 0;
 //BA.debugLineNum = 370;BA.debugLine="Sub ModelBox_itemClick (Position As Int, Value As Object)";
 //BA.debugLineNum = 371;BA.debugLine="If savephone = \"True\" Then";
if ((mostCurrent._savephone).equals("True")) { 
 //BA.debugLineNum = 372;BA.debugLine="savedphone = ModelBox.SelectedItem";
mostCurrent._savedphone = mostCurrent._modelbox.getSelectedItem();
 //BA.debugLineNum = 373;BA.debugLine="savedvariant = VariantBox.SelectedItem";
mostCurrent._savedvariant = mostCurrent._variantbox.getSelectedItem();
 };
 //BA.debugLineNum = 375;BA.debugLine="VariantBox.Clear";
mostCurrent._variantbox.Clear();
 //BA.debugLineNum = 376;BA.debugLine="VariantBox.Add(\"No variants available\")";
mostCurrent._variantbox.Add("No variants available");
 //BA.debugLineNum = 377;BA.debugLine="VariantBox.Enabled = False";
mostCurrent._variantbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 378;BA.debugLine="ShadowCheckbox.Enabled = True";
mostCurrent._shadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 379;BA.debugLine="UnderShadowCheckbox.Enabled = False";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 380;BA.debugLine="UnderShadowCheckbox.Checked = False";
mostCurrent._undershadowcheckbox.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 381;BA.debugLine="GlossCheckbox.Enabled = False";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 382;BA.debugLine="GlossCheckbox.Checked = False";
mostCurrent._glosscheckbox.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 383;BA.debugLine="Select Case ModelBox.SelectedItem";
switch (BA.switchObjectToInt(mostCurrent._modelbox.getSelectedItem(),"HTC One X, HTC One X+","HTC One","Samsung Galaxy SIII","Motorola Xoom","Google Nexus 7","Samsung Galaxy SII, Epic 4G Touch","Samsung Galaxy SIII Mini","Motorola Xoom","Samsung Google Galaxy Nexus","Google Nexus 4","Google Nexus S","HTC One S","HTC One V","HTC Desire HD, HTC Inspire 4G","Motorola Droid RAZR","Motorola Droid RAZR M")) {
case 0:
case 1:
 //BA.debugLineNum = 385;BA.debugLine="VariantSet(Array As String(\"White\", \"Black\"))";
_variantset(anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"White","Black"}));
 //BA.debugLineNum = 386;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 break;
case 2:
 //BA.debugLineNum = 388;BA.debugLine="VariantSet(Array As String(\"Blue\", \"White\", \"Black\", \"Red\", \"Brown\"))";
_variantset(anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Blue","White","Black","Red","Brown"}));
 //BA.debugLineNum = 389;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 break;
case 3:
case 4:
 //BA.debugLineNum = 391;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 392;BA.debugLine="UnderShadowCheckbox.Enabled = True";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 393;BA.debugLine="VariantSet(Array As String(\"Portrait\", \"Landscape\"))";
_variantset(anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Portrait","Landscape"}));
 break;
case 5:
 //BA.debugLineNum = 395;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 396;BA.debugLine="UnderShadowCheckbox.Enabled = True";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 397;BA.debugLine="VariantSet(Array As String(\"Galaxy SII\", \"Epic 4G Touch\", \"Galaxy SII (T-Mobile)\"))";
_variantset(anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Galaxy SII","Epic 4G Touch","Galaxy SII (T-Mobile)"}));
 break;
case 6:
 //BA.debugLineNum = 399;BA.debugLine="GlossCheckbox.Enabled = False";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 400;BA.debugLine="UnderShadowCheckbox.Enabled = False";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 401;BA.debugLine="GlossCheckbox.Checked = True";
mostCurrent._glosscheckbox.setChecked(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 402;BA.debugLine="UnderShadowCheckbox.Checked = True";
mostCurrent._undershadowcheckbox.setChecked(anywheresoftware.b4a.keywords.Common.True);
 break;
case 7:
case 8:
 //BA.debugLineNum = 404;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 405;BA.debugLine="UnderShadowCheckbox.Enabled = True";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 break;
case 9:
case 10:
case 11:
case 12:
 //BA.debugLineNum = 407;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 break;
case 13:
 //BA.debugLineNum = 409;BA.debugLine="GlossCheckbox.Checked = True";
mostCurrent._glosscheckbox.setChecked(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 410;BA.debugLine="UnderShadowCheckbox.Enabled = True";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 break;
case 14:
case 15:
 break;
}
;
 //BA.debugLineNum = 413;BA.debugLine="If savephone = \"True\" AND VariantBox.Size > 0 Then";
if ((mostCurrent._savephone).equals("True") && mostCurrent._variantbox.getSize()>0) { 
 //BA.debugLineNum = 414;BA.debugLine="For x = 0 To VariantBox.Size - 1";
{
final double step371 = 1;
final double limit371 = (int)(mostCurrent._variantbox.getSize()-1);
for (_x = (int)(0); (step371 > 0 && _x <= limit371) || (step371 < 0 && _x >= limit371); _x += step371) {
 //BA.debugLineNum = 415;BA.debugLine="If VariantBox.GetItem(x) = savedvariant Then VariantBox.SelectedIndex = x";
if ((mostCurrent._variantbox.GetItem(_x)).equals(mostCurrent._savedvariant)) { 
mostCurrent._variantbox.setSelectedIndex(_x);};
 }
};
 };
 //BA.debugLineNum = 418;BA.debugLine="StateManager.SetSetting(\"savedvariant\", savedvariant)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"savedvariant",mostCurrent._savedvariant);
 //BA.debugLineNum = 419;BA.debugLine="RefreshImage";
_refreshimage();
 //BA.debugLineNum = 420;BA.debugLine="End Sub";
return "";
}
public static String  _pager_pagechanged(int _position) throws Exception{
 //BA.debugLineNum = 359;BA.debugLine="Sub Pager_PageChanged (Position As Int)";
 //BA.debugLineNum = 360;BA.debugLine="CurrentPage = pager.CurrentPage";
_currentpage = mostCurrent._pager.getCurrentPage();
 //BA.debugLineNum = 361;BA.debugLine="StateManager.SetSetting(\"CurrentPage\", CurrentPage)";
mostCurrent._statemanager._setsetting(mostCurrent.activityBA,"CurrentPage",BA.NumberToString(_currentpage));
 //BA.debugLineNum = 362;BA.debugLine="End Sub";
return "";
}
public static String  _pager_pagecreated(int _position,Object _page) throws Exception{
anywheresoftware.b4a.objects.PanelWrapper _pan = null;
com.yttrium.scrotter.main._panelinfo _pi = null;
 //BA.debugLineNum = 288;BA.debugLine="Sub Pager_PageCreated (Position As Int, Page As Object)";
 //BA.debugLineNum = 289;BA.debugLine="Log (\"Page created \" & Position)";
anywheresoftware.b4a.keywords.Common.Log("Page created "+BA.NumberToString(_position));
 //BA.debugLineNum = 290;BA.debugLine="Dim pan As Panel";
_pan = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 291;BA.debugLine="Dim pi As PanelInfo";
_pi = new com.yttrium.scrotter.main._panelinfo();
 //BA.debugLineNum = 292;BA.debugLine="pan = Page";
_pan.setObject((android.view.ViewGroup)(_page));
 //BA.debugLineNum = 293;BA.debugLine="pi = pan.Tag";
_pi = (com.yttrium.scrotter.main._panelinfo)(_pan.getTag());
 //BA.debugLineNum = 294;BA.debugLine="Select pi.PanelType";
switch (BA.switchObjectToInt(_pi.PanelType,_type_about,_type_preview,_type_options)) {
case 0:
 //BA.debugLineNum = 296;BA.debugLine="If Not(pi.LayoutLoaded) Then";
if (anywheresoftware.b4a.keywords.Common.Not(_pi.LayoutLoaded)) { 
 //BA.debugLineNum = 297;BA.debugLine="pan.LoadLayout(\"About\")";
_pan.LoadLayout("About",mostCurrent.activityBA);
 //BA.debugLineNum = 298;BA.debugLine="pi.LayoutLoaded = True";
_pi.LayoutLoaded = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 299;BA.debugLine="ScrotterTitle.Text = \"Scrotter\"";
mostCurrent._scrottertitle.setText((Object)("Scrotter"));
 //BA.debugLineNum = 300;BA.debugLine="ScrotterTitle.TextSize = ScrotterTitle.Height * 800/1000dip";
mostCurrent._scrottertitle.setTextSize((float)(mostCurrent._scrottertitle.getHeight()*800/(double)anywheresoftware.b4a.keywords.Common.DipToCurrent((int)(1000))));
 //BA.debugLineNum = 301;BA.debugLine="ScrotterVers.Text = \"v\" & version & \" (\" & releasedate & \")\"";
mostCurrent._scrottervers.setText((Object)("v"+_version+" ("+_releasedate+")"));
 //BA.debugLineNum = 302;BA.debugLine="ScrotterVers.TextSize = ScrotterVers.Height * 500/1000dip";
mostCurrent._scrottervers.setTextSize((float)(mostCurrent._scrottervers.getHeight()*500/(double)anywheresoftware.b4a.keywords.Common.DipToCurrent((int)(1000))));
 //BA.debugLineNum = 303;BA.debugLine="Select theme";
switch (BA.switchObjectToInt(_theme,"Light","Dark")) {
case 0:
 //BA.debugLineNum = 305;BA.debugLine="aboutpage.Color = Colors.White";
mostCurrent._aboutpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 306;BA.debugLine="ScrotterTitle.TextColor = Colors.DarkGray";
mostCurrent._scrottertitle.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 307;BA.debugLine="ScrotterVers.TextColor = Colors.Gray";
mostCurrent._scrottervers.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 break;
case 1:
 //BA.debugLineNum = 309;BA.debugLine="aboutpage.Color = Colors.RGB(50, 50, 50)";
mostCurrent._aboutpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 310;BA.debugLine="ScrotterTitle.TextColor = Colors.LightGray";
mostCurrent._scrottertitle.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 311;BA.debugLine="ScrotterVers.TextColor = Colors.Gray";
mostCurrent._scrottervers.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 break;
}
;
 };
 //BA.debugLineNum = 316;BA.debugLine="Loaded(1) = True";
_loaded[(int)(1)] = anywheresoftware.b4a.keywords.Common.True;
 break;
case 1:
 //BA.debugLineNum = 318;BA.debugLine="If Not(pi.LayoutLoaded) Then";
if (anywheresoftware.b4a.keywords.Common.Not(_pi.LayoutLoaded)) { 
 //BA.debugLineNum = 319;BA.debugLine="pan.LoadLayout(\"Preview\")";
_pan.LoadLayout("Preview",mostCurrent.activityBA);
 //BA.debugLineNum = 320;BA.debugLine="pi.LayoutLoaded = True";
_pi.LayoutLoaded = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 321;BA.debugLine="Select theme";
switch (BA.switchObjectToInt(_theme,"Light","Dark")) {
case 0:
 //BA.debugLineNum = 323;BA.debugLine="previewpage.Color = Colors.White";
mostCurrent._previewpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 break;
case 1:
 //BA.debugLineNum = 325;BA.debugLine="previewpage.Color = Colors.RGB(50, 50, 50)";
mostCurrent._previewpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 break;
}
;
 };
 //BA.debugLineNum = 328;BA.debugLine="Loaded(2) = True";
_loaded[(int)(2)] = anywheresoftware.b4a.keywords.Common.True;
 break;
case 2:
 //BA.debugLineNum = 330;BA.debugLine="If Not(pi.LayoutLoaded) Then";
if (anywheresoftware.b4a.keywords.Common.Not(_pi.LayoutLoaded)) { 
 //BA.debugLineNum = 331;BA.debugLine="pan.LoadLayout(\"Options\")";
_pan.LoadLayout("Options",mostCurrent.activityBA);
 //BA.debugLineNum = 332;BA.debugLine="pi.LayoutLoaded = True";
_pi.LayoutLoaded = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 333;BA.debugLine="ModelBox.AddAll(devicelist)";
mostCurrent._modelbox.AddAll(mostCurrent._devicelist);
 //BA.debugLineNum = 334;BA.debugLine="VariantBox.Add(\"No variants available\")";
mostCurrent._variantbox.Add("No variants available");
 //BA.debugLineNum = 335;BA.debugLine="ModelBox.Prompt = \"Pick your phone\"";
mostCurrent._modelbox.setPrompt("Pick your phone");
 //BA.debugLineNum = 336;BA.debugLine="Select theme";
switch (BA.switchObjectToInt(_theme,"Light","Dark")) {
case 0:
 //BA.debugLineNum = 338;BA.debugLine="optionspage.Color = Colors.White";
mostCurrent._optionspage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 339;BA.debugLine="ModelBox.TextColor = Colors.DarkGray";
mostCurrent._modelbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 340;BA.debugLine="VariantBox.TextColor = Colors.DarkGray";
mostCurrent._variantbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 341;BA.debugLine="GlossCheckbox.TextColor = Colors.DarkGray";
mostCurrent._glosscheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 342;BA.debugLine="ShadowCheckbox.TextColor = Colors.DarkGray";
mostCurrent._shadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 343;BA.debugLine="UnderShadowCheckbox.TextColor = Colors.DarkGray";
mostCurrent._undershadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 break;
case 1:
 //BA.debugLineNum = 345;BA.debugLine="optionspage.Color = Colors.RGB(50, 50, 50)";
mostCurrent._optionspage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 346;BA.debugLine="ModelBox.TextColor = Colors.LightGray";
mostCurrent._modelbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 347;BA.debugLine="VariantBox.TextColor = Colors.LightGray";
mostCurrent._variantbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 348;BA.debugLine="GlossCheckbox.TextColor = Colors.LightGray";
mostCurrent._glosscheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 349;BA.debugLine="ShadowCheckbox.TextColor = Colors.LightGray";
mostCurrent._shadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 350;BA.debugLine="UnderShadowCheckbox.TextColor = Colors.LightGray";
mostCurrent._undershadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 break;
}
;
 //BA.debugLineNum = 352;BA.debugLine="ModelBox.Invalidate";
mostCurrent._modelbox.Invalidate();
 //BA.debugLineNum = 353;BA.debugLine="VariantBox.Invalidate";
mostCurrent._variantbox.Invalidate();
 };
 //BA.debugLineNum = 355;BA.debugLine="Loaded(3) = True";
_loaded[(int)(3)] = anywheresoftware.b4a.keywords.Common.True;
 break;
}
;
 //BA.debugLineNum = 357;BA.debugLine="End Sub";
return "";
}
public static boolean  _parsesavephone() throws Exception{
 //BA.debugLineNum = 732;BA.debugLine="Sub ParseSavePhone As Boolean";
 //BA.debugLineNum = 733;BA.debugLine="If savephone = \"False\" Then Return False Else Return True";
if ((mostCurrent._savephone).equals("False")) { 
if (true) return anywheresoftware.b4a.keywords.Common.False;}
else {
if (true) return anywheresoftware.b4a.keywords.Common.True;};
 //BA.debugLineNum = 734;BA.debugLine="End Sub";
return false;
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 14;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 17;BA.debugLine="Dim TYPE_ABOUT As Int : TYPE_ABOUT = 1";
_type_about = 0;
 //BA.debugLineNum = 17;BA.debugLine="Dim TYPE_ABOUT As Int : TYPE_ABOUT = 1";
_type_about = (int)(1);
 //BA.debugLineNum = 18;BA.debugLine="Dim TYPE_PREVIEW As Int : TYPE_PREVIEW = 2";
_type_preview = 0;
 //BA.debugLineNum = 18;BA.debugLine="Dim TYPE_PREVIEW As Int : TYPE_PREVIEW = 2";
_type_preview = (int)(2);
 //BA.debugLineNum = 19;BA.debugLine="Dim TYPE_OPTIONS As Int : TYPE_OPTIONS = 3";
_type_options = 0;
 //BA.debugLineNum = 19;BA.debugLine="Dim TYPE_OPTIONS As Int : TYPE_OPTIONS = 3";
_type_options = (int)(3);
 //BA.debugLineNum = 20;BA.debugLine="Dim FILL_PARENT As Int : FILL_PARENT = -1";
_fill_parent = 0;
 //BA.debugLineNum = 20;BA.debugLine="Dim FILL_PARENT As Int : FILL_PARENT = -1";
_fill_parent = (int)(-1);
 //BA.debugLineNum = 21;BA.debugLine="Dim WRAP_CONTENT As Int : WRAP_CONTENT = -2";
_wrap_content = 0;
 //BA.debugLineNum = 21;BA.debugLine="Dim WRAP_CONTENT As Int : WRAP_CONTENT = -2";
_wrap_content = (int)(-2);
 //BA.debugLineNum = 22;BA.debugLine="Type PanelInfo (PanelType As Int, LayoutLoaded As Boolean)";
;
 //BA.debugLineNum = 23;BA.debugLine="Dim CurrentPage As Int = 1";
_currentpage = (int)(1);
 //BA.debugLineNum = 24;BA.debugLine="Dim version As String = \"0.9\"";
_version = "0.9";
 //BA.debugLineNum = 25;BA.debugLine="Dim releasedate As String = \"6/17/2013\"";
_releasedate = "6/17/2013";
 //BA.debugLineNum = 26;BA.debugLine="Dim theme As String";
_theme = "";
 //BA.debugLineNum = 27;BA.debugLine="Dim Loaded(4) As Boolean";
_loaded = new boolean[(int)(4)];
;
 //BA.debugLineNum = 28;BA.debugLine="Dim PrefScreen As AHPreferenceScreen";
_prefscreen = new anywheresoftware.b4a.objects.preferenceactivity.PreferenceScreenWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Dim PrefManager As AHPreferenceManager";
_prefmanager = new anywheresoftware.b4a.objects.preferenceactivity.PreferenceManager();
 //BA.debugLineNum = 30;BA.debugLine="End Sub";
return "";
}
public static String  _refreshimage() throws Exception{
 //BA.debugLineNum = 422;BA.debugLine="Sub RefreshImage";
 //BA.debugLineNum = 423;BA.debugLine="Loading.Visible = True";
mostCurrent._loading.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 424;BA.debugLine="If BackgroundThread.Running = True Then";
if (mostCurrent._backgroundthread.getRunning()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 425;BA.debugLine="BackgroundThread.Interrupt";
mostCurrent._backgroundthread.Interrupt();
 };
 //BA.debugLineNum = 427;BA.debugLine="BackgroundThread.Start(Me, \"ImageProcess\", Null)";
mostCurrent._backgroundthread.Start(main.getObject(),"ImageProcess",(Object[])(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 428;BA.debugLine="pager.PagingEnabled = False";
mostCurrent._pager.setPagingEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 429;BA.debugLine="Loadbtn.Enabled = True";
mostCurrent._loadbtn.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 430;BA.debugLine="SaveBtn.Enabled = True";
mostCurrent._savebtn.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 431;BA.debugLine="End Sub";
return "";
}
public static String  _refreshtheme() throws Exception{
int _y = 0;
anywheresoftware.b4a.objects.collections.List _z = null;
int _count = 0;
 //BA.debugLineNum = 135;BA.debugLine="Sub RefreshTheme";
 //BA.debugLineNum = 136;BA.debugLine="Select theme";
switch (BA.switchObjectToInt(_theme,"Light","Dark")) {
case 0:
 //BA.debugLineNum = 138;BA.debugLine="tabs.Color = Colors.White";
mostCurrent._tabs.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 139;BA.debugLine="tabs.BackgroundColorPressed = Colors.DarkGray";
mostCurrent._tabs.setBackgroundColorPressed(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 140;BA.debugLine="tabs.LineColorCenter = Colors.DarkGray";
mostCurrent._tabs.setLineColorCenter(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 141;BA.debugLine="tabs.TextColor = Colors.LightGray";
mostCurrent._tabs.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 142;BA.debugLine="tabs.TextColorCenter = Colors.DarkGray";
mostCurrent._tabs.setTextColorCenter(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 143;BA.debugLine="tabs.Invalidate";
mostCurrent._tabs.Invalidate();
 //BA.debugLineNum = 144;BA.debugLine="If Loaded(1) = True Then";
if (_loaded[(int)(1)]==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 145;BA.debugLine="aboutpage.Color = Colors.White";
mostCurrent._aboutpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 146;BA.debugLine="ScrotterTitle.TextColor = Colors.DarkGray";
mostCurrent._scrottertitle.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 147;BA.debugLine="ScrotterVers.TextColor = Colors.Gray";
mostCurrent._scrottervers.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 148;BA.debugLine="SettingsBtn.TextColor = Colors.DarkGray";
mostCurrent._settingsbtn.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 };
 //BA.debugLineNum = 150;BA.debugLine="If Loaded(2) = True Then previewpage.Color = Colors.White";
if (_loaded[(int)(2)]==anywheresoftware.b4a.keywords.Common.True) { 
mostCurrent._previewpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);};
 //BA.debugLineNum = 151;BA.debugLine="If Loaded(3) = True Then";
if (_loaded[(int)(3)]==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 152;BA.debugLine="optionspage.Color = Colors.White";
mostCurrent._optionspage.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 153;BA.debugLine="ModelBox.TextColor = Colors.DarkGray";
mostCurrent._modelbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 154;BA.debugLine="VariantBox.TextColor = Colors.DarkGray";
mostCurrent._variantbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 155;BA.debugLine="GlossCheckbox.TextColor = Colors.DarkGray";
mostCurrent._glosscheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 156;BA.debugLine="ShadowCheckbox.TextColor = Colors.DarkGray";
mostCurrent._shadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 157;BA.debugLine="UnderShadowCheckbox.TextColor = Colors.DarkGray";
mostCurrent._undershadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.DarkGray);
 //BA.debugLineNum = 158;BA.debugLine="Dim y As Int = ModelBox.SelectedIndex";
_y = mostCurrent._modelbox.getSelectedIndex();
 //BA.debugLineNum = 159;BA.debugLine="ModelBox.Clear";
mostCurrent._modelbox.Clear();
 //BA.debugLineNum = 160;BA.debugLine="ModelBox.AddAll(devicelist)";
mostCurrent._modelbox.AddAll(mostCurrent._devicelist);
 //BA.debugLineNum = 161;BA.debugLine="ModelBox.SelectedIndex = y";
mostCurrent._modelbox.setSelectedIndex(_y);
 //BA.debugLineNum = 162;BA.debugLine="If VariantBox.Size > 0 Then";
if (mostCurrent._variantbox.getSize()>0) { 
 //BA.debugLineNum = 163;BA.debugLine="y = VariantBox.SelectedIndex";
_y = mostCurrent._variantbox.getSelectedIndex();
 //BA.debugLineNum = 164;BA.debugLine="Dim z As List";
_z = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 165;BA.debugLine="z.Initialize";
_z.Initialize();
 //BA.debugLineNum = 166;BA.debugLine="For count = 0 To VariantBox.Size - 1";
{
final double step134 = 1;
final double limit134 = (int)(mostCurrent._variantbox.getSize()-1);
for (_count = (int)(0); (step134 > 0 && _count <= limit134) || (step134 < 0 && _count >= limit134); _count += step134) {
 //BA.debugLineNum = 167;BA.debugLine="z.Add(VariantBox.GetItem(count))";
_z.Add((Object)(mostCurrent._variantbox.GetItem(_count)));
 }
};
 //BA.debugLineNum = 169;BA.debugLine="VariantBox.Clear";
mostCurrent._variantbox.Clear();
 //BA.debugLineNum = 170;BA.debugLine="VariantBox.AddAll(z)";
mostCurrent._variantbox.AddAll(_z);
 //BA.debugLineNum = 171;BA.debugLine="VariantBox.SelectedIndex = y";
mostCurrent._variantbox.setSelectedIndex(_y);
 };
 };
 break;
case 1:
 //BA.debugLineNum = 175;BA.debugLine="tabs.Color = Colors.RGB(50, 50, 50)";
mostCurrent._tabs.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 176;BA.debugLine="tabs.BackgroundColorPressed = Colors.White";
mostCurrent._tabs.setBackgroundColorPressed(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 177;BA.debugLine="tabs.LineColorCenter = Colors.LightGray";
mostCurrent._tabs.setLineColorCenter(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 178;BA.debugLine="tabs.TextColor = Colors.Gray";
mostCurrent._tabs.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 179;BA.debugLine="tabs.TextColorCenter = Colors.LightGray";
mostCurrent._tabs.setTextColorCenter(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 180;BA.debugLine="tabs.Invalidate";
mostCurrent._tabs.Invalidate();
 //BA.debugLineNum = 181;BA.debugLine="If Loaded(1) = True Then";
if (_loaded[(int)(1)]==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 182;BA.debugLine="aboutpage.Color = Colors.RGB(50, 50, 50)";
mostCurrent._aboutpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 183;BA.debugLine="ScrotterTitle.TextColor = Colors.LightGray";
mostCurrent._scrottertitle.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 184;BA.debugLine="ScrotterVers.TextColor = Colors.Gray";
mostCurrent._scrottervers.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 185;BA.debugLine="SettingsBtn.TextColor = Colors.LightGray";
mostCurrent._settingsbtn.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 };
 //BA.debugLineNum = 187;BA.debugLine="If Loaded(2) = True Then  previewpage.Color = Colors.RGB(50, 50, 50)";
if (_loaded[(int)(2)]==anywheresoftware.b4a.keywords.Common.True) { 
mostCurrent._previewpage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));};
 //BA.debugLineNum = 188;BA.debugLine="If Loaded(3) = True Then";
if (_loaded[(int)(3)]==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 189;BA.debugLine="optionspage.Color = Colors.RGB(50, 50, 50)";
mostCurrent._optionspage.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(50),(int)(50),(int)(50)));
 //BA.debugLineNum = 190;BA.debugLine="ModelBox.TextColor = Colors.LightGray";
mostCurrent._modelbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 191;BA.debugLine="VariantBox.TextColor = Colors.LightGray";
mostCurrent._variantbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 192;BA.debugLine="GlossCheckbox.TextColor = Colors.LightGray";
mostCurrent._glosscheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 193;BA.debugLine="ShadowCheckbox.TextColor = Colors.LightGray";
mostCurrent._shadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 194;BA.debugLine="UnderShadowCheckbox.TextColor = Colors.LightGray";
mostCurrent._undershadowcheckbox.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.LightGray);
 //BA.debugLineNum = 195;BA.debugLine="Dim y As Int = ModelBox.SelectedIndex";
_y = mostCurrent._modelbox.getSelectedIndex();
 //BA.debugLineNum = 196;BA.debugLine="ModelBox.Clear";
mostCurrent._modelbox.Clear();
 //BA.debugLineNum = 197;BA.debugLine="ModelBox.AddAll(devicelist)";
mostCurrent._modelbox.AddAll(mostCurrent._devicelist);
 //BA.debugLineNum = 198;BA.debugLine="ModelBox.SelectedIndex = y";
mostCurrent._modelbox.setSelectedIndex(_y);
 //BA.debugLineNum = 199;BA.debugLine="If VariantBox.Size > 0 Then";
if (mostCurrent._variantbox.getSize()>0) { 
 //BA.debugLineNum = 200;BA.debugLine="y = VariantBox.SelectedIndex";
_y = mostCurrent._variantbox.getSelectedIndex();
 //BA.debugLineNum = 201;BA.debugLine="Dim z As List";
_z = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 202;BA.debugLine="z.Initialize";
_z.Initialize();
 //BA.debugLineNum = 203;BA.debugLine="For count = 0 To VariantBox.Size - 1";
{
final double step171 = 1;
final double limit171 = (int)(mostCurrent._variantbox.getSize()-1);
for (_count = (int)(0); (step171 > 0 && _count <= limit171) || (step171 < 0 && _count >= limit171); _count += step171) {
 //BA.debugLineNum = 204;BA.debugLine="z.Add(VariantBox.GetItem(count))";
_z.Add((Object)(mostCurrent._variantbox.GetItem(_count)));
 }
};
 //BA.debugLineNum = 206;BA.debugLine="VariantBox.Clear";
mostCurrent._variantbox.Clear();
 //BA.debugLineNum = 207;BA.debugLine="VariantBox.AddAll(z)";
mostCurrent._variantbox.AddAll(_z);
 //BA.debugLineNum = 208;BA.debugLine="VariantBox.SelectedIndex = y";
mostCurrent._variantbox.setSelectedIndex(_y);
 };
 };
 break;
}
;
 //BA.debugLineNum = 212;BA.debugLine="ScrotterTitle.Typeface = UbuntuRegular";
mostCurrent._scrottertitle.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 213;BA.debugLine="ScrotterVers.Typeface = UbuntuRegular";
mostCurrent._scrottervers.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 214;BA.debugLine="Loadbtn.Typeface = UbuntuRegular";
mostCurrent._loadbtn.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 215;BA.debugLine="SaveBtn.Typeface = UbuntuRegular";
mostCurrent._savebtn.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 216;BA.debugLine="GlossCheckbox.Typeface = UbuntuRegular";
mostCurrent._glosscheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 217;BA.debugLine="ShadowCheckbox.Typeface = UbuntuRegular";
mostCurrent._shadowcheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 218;BA.debugLine="UnderShadowCheckbox.Typeface = UbuntuRegular";
mostCurrent._undershadowcheckbox.setTypeface((android.graphics.Typeface)(mostCurrent._ubunturegular.getObject()));
 //BA.debugLineNum = 219;BA.debugLine="End Sub";
return "";
}
public static anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper  _resizeimage(anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _original,int _targetx,int _targety) throws Exception{
float _origratio = 0f;
float _targetratio = 0f;
float _scale = 0f;
anywheresoftware.b4a.objects.drawable.CanvasWrapper _c = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _b = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper _r = null;
int _w = 0;
int _h = 0;
com.AB.ABExtDrawing.ABExtDrawing _extdraw = null;
com.AB.ABExtDrawing.ABExtDrawing.ABPaint _paint = null;
 //BA.debugLineNum = 665;BA.debugLine="Sub ResizeImage(original As Bitmap, TargetX As Int, TargetY As Int) As Bitmap";
 //BA.debugLineNum = 666;BA.debugLine="Dim origRatio As Float = original.Width / original.Height";
_origratio = (float)(_original.getWidth()/(double)_original.getHeight());
 //BA.debugLineNum = 667;BA.debugLine="Dim targetRatio As Float = TargetX / TargetY";
_targetratio = (float)(_targetx/(double)_targety);
 //BA.debugLineNum = 668;BA.debugLine="Dim scale As Float";
_scale = 0f;
 //BA.debugLineNum = 669;BA.debugLine="If targetRatio > origRatio Then";
if (_targetratio>_origratio) { 
 //BA.debugLineNum = 670;BA.debugLine="scale = TargetY / original.Height";
_scale = (float)(_targety/(double)_original.getHeight());
 }else {
 //BA.debugLineNum = 672;BA.debugLine="scale = TargetX / original.Width";
_scale = (float)(_targetx/(double)_original.getWidth());
 };
 //BA.debugLineNum = 674;BA.debugLine="Dim C As Canvas";
_c = new anywheresoftware.b4a.objects.drawable.CanvasWrapper();
 //BA.debugLineNum = 675;BA.debugLine="Dim b As Bitmap";
_b = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 676;BA.debugLine="b.InitializeMutable(TargetX, TargetY)";
_b.InitializeMutable(_targetx,_targety);
 //BA.debugLineNum = 677;BA.debugLine="C.Initialize2(b)";
_c.Initialize2((android.graphics.Bitmap)(_b.getObject()));
 //BA.debugLineNum = 678;BA.debugLine="C.DrawColor(Colors.Transparent)";
_c.DrawColor(anywheresoftware.b4a.keywords.Common.Colors.Transparent);
 //BA.debugLineNum = 679;BA.debugLine="Dim R As Rect";
_r = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper();
 //BA.debugLineNum = 680;BA.debugLine="Dim w = original.Width * scale, h = original.Height * scale As Int";
_w = (int)(_original.getWidth()*_scale);
_h = (int)(_original.getHeight()*_scale);
 //BA.debugLineNum = 681;BA.debugLine="R.Initialize(TargetX/2-w/2, TargetY/2-h/2, TargetX/2+w/2, TargetY/2+h/2)";
_r.Initialize((int)(_targetx/(double)2-_w/(double)2),(int)(_targety/(double)2-_h/(double)2),(int)(_targetx/(double)2+_w/(double)2),(int)(_targety/(double)2+_h/(double)2));
 //BA.debugLineNum = 682;BA.debugLine="Dim ExtDraw As ABExtDrawing";
_extdraw = new com.AB.ABExtDrawing.ABExtDrawing();
 //BA.debugLineNum = 683;BA.debugLine="Dim paint As ABPaint";
_paint = new com.AB.ABExtDrawing.ABExtDrawing.ABPaint();
 //BA.debugLineNum = 684;BA.debugLine="paint.Initialize()";
_paint.Initialize();
 //BA.debugLineNum = 685;BA.debugLine="paint.setFilterBitmap(True)";
_paint.SetFilterBitmap(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 686;BA.debugLine="paint.SetAntiAlias(True)";
_paint.SetAntiAlias(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 687;BA.debugLine="ExtDraw.drawBitmap(C, original, Null, R, paint)";
_extdraw.drawBitmap(_c,(android.graphics.Bitmap)(_original.getObject()),(android.graphics.Rect)(anywheresoftware.b4a.keywords.Common.Null),(android.graphics.Rect)(_r.getObject()),_paint);
 //BA.debugLineNum = 688;BA.debugLine="Return b";
if (true) return _b;
 //BA.debugLineNum = 689;BA.debugLine="End Sub";
return null;
}
public static String  _savebtn_click() throws Exception{
 //BA.debugLineNum = 433;BA.debugLine="Sub SaveBtn_Click";
 //BA.debugLineNum = 434;BA.debugLine="Msgbox(\"Saving disabled in developer previews.\", \"Sorry!\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Saving disabled in developer previews.","Sorry!",mostCurrent.activityBA);
 //BA.debugLineNum = 458;BA.debugLine="End Sub";
return "";
}
public static String  _setdefaults() throws Exception{
 //BA.debugLineNum = 127;BA.debugLine="Sub SetDefaults";
 //BA.debugLineNum = 129;BA.debugLine="PrefManager.SetBoolean(\"check1\", True)";
_prefmanager.SetBoolean("check1",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 130;BA.debugLine="PrefManager.SetBoolean(\"check2\", False)";
_prefmanager.SetBoolean("check2",anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 131;BA.debugLine="PrefManager.SetString(\"edit1\", \"Hello!\")";
_prefmanager.SetString("edit1","Hello!");
 //BA.debugLineNum = 132;BA.debugLine="PrefManager.SetString(\"list1\", \"Black\")";
_prefmanager.SetString("list1","Black");
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _settingsbtn_click() throws Exception{
 //BA.debugLineNum = 752;BA.debugLine="Sub SettingsBtn_Click";
 //BA.debugLineNum = 753;BA.debugLine="StartActivity(PrefScreen.CreateIntent)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_prefscreen.CreateIntent()));
 //BA.debugLineNum = 754;BA.debugLine="End Sub";
return "";
}
public static String  _shadowcheckbox_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 726;BA.debugLine="Sub ShadowCheckbox_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 727;BA.debugLine="RefreshImage";
_refreshimage();
 //BA.debugLineNum = 728;BA.debugLine="End Sub";
return "";
}
public static String  _undershadowcheckbox_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 729;BA.debugLine="Sub UnderShadowCheckbox_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 730;BA.debugLine="RefreshImage";
_refreshimage();
 //BA.debugLineNum = 731;BA.debugLine="End Sub";
return "";
}
public static String  _variantbox_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 691;BA.debugLine="Sub VariantBox_ItemClick (Position As Int, Value As Object)";
 //BA.debugLineNum = 692;BA.debugLine="If ModelBox.SelectedItem = \"Samsung Galaxy SIII\" Then";
if ((mostCurrent._modelbox.getSelectedItem()).equals("Samsung Galaxy SIII")) { 
 //BA.debugLineNum = 693;BA.debugLine="If (VariantBox.SelectedItem = \"Black\" OR VariantBox.SelectedItem = \"Brown\" OR VariantBox.SelectedItem = \"Red\") Then";
if (((mostCurrent._variantbox.getSelectedItem()).equals("Black") || (mostCurrent._variantbox.getSelectedItem()).equals("Brown") || (mostCurrent._variantbox.getSelectedItem()).equals("Red"))) { 
 //BA.debugLineNum = 694;BA.debugLine="GlossCheckbox.Enabled = False";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 695;BA.debugLine="GlossCheckbox.Checked = True";
mostCurrent._glosscheckbox.setChecked(anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 697;BA.debugLine="If GlossCheckbox.Enabled = False Then";
if (mostCurrent._glosscheckbox.getEnabled()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 698;BA.debugLine="GlossCheckbox.Enabled = True";
mostCurrent._glosscheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 699;BA.debugLine="GlossCheckbox.Checked = False";
mostCurrent._glosscheckbox.setChecked(anywheresoftware.b4a.keywords.Common.False);
 };
 };
 }else if((mostCurrent._modelbox.getSelectedItem()).equals("Samsung Galaxy SII, Epic 4G Touch")) { 
 //BA.debugLineNum = 703;BA.debugLine="If VariantBox.SelectedItem = \"Galaxy SII (T-Mobile)\" Then";
if ((mostCurrent._variantbox.getSelectedItem()).equals("Galaxy SII (T-Mobile)")) { 
 //BA.debugLineNum = 704;BA.debugLine="UnderShadowCheckbox.Enabled = False";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 705;BA.debugLine="UnderShadowCheckbox.Checked = False";
mostCurrent._undershadowcheckbox.setChecked(anywheresoftware.b4a.keywords.Common.False);
 }else {
 //BA.debugLineNum = 707;BA.debugLine="UnderShadowCheckbox.Enabled = True";
mostCurrent._undershadowcheckbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 };
 //BA.debugLineNum = 710;BA.debugLine="If savephone = \"True\" Then savedvariant = VariantBox.SelectedItem";
if ((mostCurrent._savephone).equals("True")) { 
mostCurrent._savedvariant = mostCurrent._variantbox.getSelectedItem();};
 //BA.debugLineNum = 711;BA.debugLine="RefreshImage";
_refreshimage();
 //BA.debugLineNum = 712;BA.debugLine="End Sub";
return "";
}
public static String  _variantset(anywheresoftware.b4a.objects.collections.List _var) throws Exception{
 //BA.debugLineNum = 364;BA.debugLine="Sub VariantSet (var As List)";
 //BA.debugLineNum = 365;BA.debugLine="If VariantBox.Enabled = False Then VariantBox.Enabled = True";
if (mostCurrent._variantbox.getEnabled()==anywheresoftware.b4a.keywords.Common.False) { 
mostCurrent._variantbox.setEnabled(anywheresoftware.b4a.keywords.Common.True);};
 //BA.debugLineNum = 366;BA.debugLine="VariantBox.Clear";
mostCurrent._variantbox.Clear();
 //BA.debugLineNum = 367;BA.debugLine="VariantBox.AddAll(var)";
mostCurrent._variantbox.AddAll(_var);
 //BA.debugLineNum = 368;BA.debugLine="End Sub";
return "";
}
}