package com.jackmeng.core.gui.childs;

import com.jackmeng.const_Global;
import com.jackmeng.core.use_HalcyonCore;
import com.jackmeng.core.use_HalcyonFolder;
import com.jackmeng.core.abst.impl_App;
import com.jackmeng.core.abst.impl_HalcyonRefreshable;
import com.jackmeng.core.gui.const_Manager;
import com.jackmeng.core.gui.const_ResourceManager;
import com.jackmeng.core.gui.gui_HalcyonMoreApps;
import com.jackmeng.core.gui.gui_HalcyonPlaylistSelect;
import com.jackmeng.core.ploogin.impl_Ploogin;
import com.jackmeng.core.use_HalcyonFolder.halcyonfolder_Content;
import com.jackmeng.sys.pstream;
import com.jackmeng.sys.use_Task;
import com.jackmeng.util.const_GeneralStatus;
import com.jackmeng.util.use_Color;
import com.jackmeng.util.use_Image;
import com.jackmeng.util.use_ResourceFetcher;
import com.jackmeng.util.use_Struct.struct_Pair;

import javax.swing.*;

import static com.jackmeng.core.gui.const_Lang.*;

import java.awt.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

public class dgui_HalcyonApps
    extends JPanel
    implements impl_HalcyonRefreshable< struct_Pair< Optional< String >, Optional< impl_App > > >
{

  private transient gui_HalcyonMoreApps apps;
  private transient gui_HalcyonPlaylistSelect fileChooser;
  private transient Map< String, JButton > appMap; // Key: AppID Value: GUIComponent

  public dgui_HalcyonApps()
  {
    appMap = new Hashtable<>();
    /*----------------------------------------------------------------------------- /
    / !!: make the current dir be able to dynamic or where the user last selected /
    /------------------------------------------------------------------------------*/
    use_HalcyonFolder.FOLDER.deserialize(halcyonfolder_Content.PLAYLIST_SELECT_FOLDER_CACHE_f.val,
        gui_HalcyonPlaylistSelect.class, x -> { // on error
          use_HalcyonFolder.FOLDER.log(x);
          fileChooser = new gui_HalcyonPlaylistSelect(use_HalcyonCore.getInheritableFrame(), // dont use
                                                                                             // Halcyon.main.expose()
                                                                                             // because you cant call
                                                                                             // when u are still
                                                                                             // constructing that obj
              ".");
          fileChooser.setListener(const_Global::append_to_Playlist);
        }, x -> { // default promise
          fileChooser = x == null ? new gui_HalcyonPlaylistSelect(use_HalcyonCore.getInheritableFrame(),
              ".") : x;
          fileChooser.setListener(const_Global::append_to_Playlist);
        });

    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> use_HalcyonFolder.FOLDER
            .serialize(halcyonfolder_Content.PLAYLIST_SELECT_FOLDER_CACHE_f.val,
                fileChooser)));

    apps = new gui_HalcyonMoreApps();

    setPreferredSize(new Dimension(const_Manager.DGUI_APPS_WIDTH, const_Manager.FRAME_MIN_HEIGHT / 2));
    setMinimumSize(getPreferredSize());
    setMaximumSize(new Dimension(const_Manager.DGUI_APPS_WIDTH + 20, const_Manager.FRAME_MIN_HEIGHT / 2));
    const_Global.APPS_POOL.addRefreshable(this);
    setPreferredSize(new Dimension(const_Manager.DGUI_APPS_WIDTH, const_Manager.FRAME_MIN_HEIGHT / 2));
    setMinimumSize(getPreferredSize());
    setMaximumSize(new Dimension(const_Manager.DGUI_APPS_WIDTH + 20, const_Manager.FRAME_MIN_HEIGHT / 2));
    setLayout(new FlowLayout(FlowLayout.CENTER, const_Manager.DGUI_APPS_APPS_ICON_HGAP,
        const_Manager.DGUI_APPS_APPS_ICON_VGAP));
    setFont(use_HalcyonCore.regularFont());

    if (const_Manager.DEBUG_GRAPHICS)
    {
      setOpaque(true);
      setBackground(use_Color.rndColor());
    }

    // =========================================================================
    // This part designates the default created pool of apps to be added
    // =========================================================================
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(_lang(LANG_APPS_ADD_PLAYLIST_TOOLTIP), fileChooser,
            const_ResourceManager.DGUI_APPS_ADD_PLAYLIST));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(_lang(LANG_APPS_OPEN_LIKED_LIST), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_PLAYER_LIKED_MUSIC));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(_lang(LANG_APPS_AUDIO_CTRLERS), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_AUDIO_CTRLER));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(_lang(LANG_APPS_OPEN_MINI_PLAYER), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_MINI_PLAYER));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(
            _lang(LANG_APPS_OPEN_SETTINGS), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_PLAYER_SETTINGS));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(
            _lang(LANG_APPS_PLAYLIST_VIEWER), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_PLAYER_LISTVIEW));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(
            _lang(LANG_APPS_REFRESH_PLAYLISTS), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_PLAYER_REFRESH));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(
            _lang(LANG_APPS_INFO), use_HalcyonCore::do_nothing,
            const_ResourceManager.DGUI_APPS_PLAYER_INFO));
    const_Global.APPS_POOL.addPoolObject(
        make_DefaultApp(_lang(LANG_APPS_VIEW_MORE), apps,
            const_ResourceManager.DGUI_APPS_PLAYER_MOREAPPS));
    /*---------------------------------------------- /
    / this part forces everything else to be ignored /
    /-----------------------------------------------*/
  }

  /**
   * @param r
   */
  private void addApp(impl_App r)
  {
    if (!(r instanceof impl_Ploogin) && !appMap.containsKey(r.id()))
    {
      use_Task.run_Snb_1(() -> {
        JButton btn = new JButton();
        btn.setToolTipText(r.toolTip());
        btn.setIcon(r.icon());
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.addActionListener(e -> r.run());
        if (!const_Manager.DEBUG_GRAPHICS)
        {
          btn.setBackground(null);
          btn.setRolloverEnabled(false);
          btn.setContentAreaFilled(false);
        }
        else
        {
          btn.setBackground(use_Color.rndColor());
        }
        r.rolloverIcon().ifPresent(rol -> {
          btn.setRolloverEnabled(true);
          btn.setRolloverIcon(rol);
        });
        appMap.put(r.id(), btn);
        add(btn);
        revalidate();
      });
    }
    else
      pstream.log.warn("Failed to add an instance of impl_App.\nRequirements checked: \n\t1. impl_Ploogin "
          + (r instanceof impl_Ploogin) + "\n\t2. appMap containment " + (appMap.containsKey(r.id())));
  }

  private void removeApp(impl_App r)
  {
    if (appMap.containsKey(r.id()))
    {
      JButton e = appMap.get(r.id());
      remove(e);
      revalidate();
    }
  }

  /**
   * @param tooltip
   * @param run
   * @param iconLocale
   * @return impl_App
   */
  public static impl_App make_DefaultApp(String tooltip, Runnable run, String iconLocale)
  {
    return new impl_App() {
      @Override
      public void run()
      {
        run.run();
      }

      @Override
      public ImageIcon icon()
      {
        return use_Image.resize_fast_1(const_Manager.DGUI_APPS_ICON_BTN_WIDTH, const_Manager.DGUI_APPS_ICON_BTN_WIDTH,
            use_ResourceFetcher.fetcher.getFromAsImageIcon(iconLocale));
      }

      @Override
      public String toolTip()
      {
        return tooltip;
      }

      @Override
      public Optional< ImageIcon > rolloverIcon()
      {
        return Optional.of(icon());
      }

      @Override
      public String id()
      {
        return new File(iconLocale).getName();
      }
    };
  }

  /**
   * @param refreshed
   */
  @Override
  public void refresh(const_GeneralStatus type, struct_Pair< Optional< String >, Optional< impl_App > > refreshed)
  {
    refreshed.second.ifPresent(type == const_GeneralStatus.ADDITION ? this::addApp : this::removeApp);
  }

  @Override
  public void dry_refresh()
  {
    const_Global.APPS_POOL.objs().forEach(x -> addApp(const_Global.APPS_POOL.get(x)));
  }
}