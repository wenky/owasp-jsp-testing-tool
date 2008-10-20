/*
 * Copyright (c) 2008, Jason Li
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer; (2) Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution; (3) Neither the name of OWASP nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.owasp.jsptester.conf.tagproperties.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.owasp.jsptester.conf.TagProperties;
import org.owasp.jsptester.parser.TagFileParser;
import org.xml.sax.SAXException;

/**
 * GUI to facilitate easy editing of tag property configuration files.
 * 
 * @author Jason Li
 * 
 */
public class TagPropertiesEditor extends JFrame
{

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger( TagPropertiesEditor.class.getName() );

    /**
     * Title for the window
     */
    private static final String TITLE = "OWASP JSP Testing Tool - Tag Properties Editor";

    /**
     * Filter for TLD files
     */
    private static final FileFilter TLD_FILE_FILTER = new FileFilter()
    {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept( File f )
        {
            return f.isDirectory() || f.getName().endsWith( ".tld" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription()
        {
            return "Tag Library Description file";
        }

    };

    /**
     * Filter for tag property configuration file
     */
    private static final FileFilter TPX_FILE_FILTER = new FileFilter()
    {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept( File f )
        {
            return f.isDirectory() || f.getName().endsWith( ".tpx" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription()
        {
            return "Tag Properties XML file";
        }

    };

    /**
     * Currently loaded tag library
     */
    private TagLibraryInfo tldInfo = null;

    /**
     * Currently loaded tag property configuration file
     */
    private File tagFile = null;

    /**
     * Whether or not changes have been made to the tag properties
     */
    private boolean dirty = false;

    /**
     * The tag properties
     */
    private final TagProperties tagProperties = new TagProperties();

    /**
     * List model of tags in the currently loaded tag library
     */
    private final DefaultListModel tagListModel = new DefaultListModel();

    /**
     * The panel for editing the current tag's configured properties
     */
    private final JPanel tagPanel = new JPanel();

    /**
     * The file chooser used to save/open files
     */
    private final JFileChooser fileChooser = new JFileChooser();

    /**
     * Creates an instance of the <code>TagPropertiesEditor</code>
     */
    public TagPropertiesEditor()
    {
        super( TITLE );

        // set the layout of the tag panel
        tagPanel.setLayout( new GridBagLayout() );

        // set the menubar
        setJMenuBar( buildMenuBar() );

        // create the list of tags
        final JList tagList = new JList( tagListModel );

        // use a custom renderer to show the tag name
        tagList.setCellRenderer( new TagInfoListCellRenderer( tagList
                .getCellRenderer() ) );

        // only one tag can be selected at a time
        tagList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        // add a listener to modify the tag panel when a new tag is selected
        tagList.addListSelectionListener( new ListSelectionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
             */
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() )
                {
                    populateTagPanel( (TagInfo) tagList.getSelectedValue() );
                }
            }
        } );

        // create a split panel with the tag list and the tag panel
        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        splitPane.setLeftComponent( new JScrollPane( tagList ) );
        splitPane.setRightComponent( new JScrollPane( tagPanel ) );
        getContentPane().add( splitPane );

        // do not do terminate daemon thread on window close
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

        // instead, add a window listener to confirm exiting the app
        addWindowListener( new WindowAdapter()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            public void windowClosing( WindowEvent e )
            {
                exitApp();

            }

        } );

        // cause validation and layout of current components
        pack();

        // set default appearance
        splitPane.setDividerLocation( 0.45 );
        setSize( 425, 300 );
    }

    /**
     * Returns a <code>JMenuBar</code> populated with all the menu items for
     * the tag properties editor GUI
     * 
     * @return a <code>JMenuBar</code> populated with all the menu items for
     *         the tag properties editor GUI
     */
    private JMenuBar buildMenuBar()
    {

        // create the Open menu item
        JMenuItem open = new JMenuItem( "Open" );
        open.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                open();
            }

        } );

        // create the Save menu item
        JMenuItem save = new JMenuItem( "Save" );
        save.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                save();
            }

        } );

        // create the Save As menu item
        JMenuItem saveAs = new JMenuItem( "Save As" );
        saveAs.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                saveAs();
            }

        } );

        // create the Load TLD menu item
        JMenuItem load = new JMenuItem( "Load TLD" );
        load.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                load();
            }

        } );

        // create the Exit menu item
        JMenuItem exit = new JMenuItem( "Exit" );
        exit.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                exitApp();
            }

        } );

        // create the File menu
        JMenu fileMenu = new JMenu( "File" );
        fileMenu.add( open );
        fileMenu.add( save );
        fileMenu.add( saveAs );
        fileMenu.addSeparator();
        fileMenu.add( load );
        fileMenu.addSeparator();
        fileMenu.add( exit );

        // create the Help menu
        JMenu helpMenu = new JMenu( "Help" );

        // create the About menu item
        JMenuItem about = new JMenuItem( "About" );
        about.addActionListener( new ActionListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed( ActionEvent e )
            {
                // TODO: create a better about box
                JOptionPane.showMessageDialog( TagPropertiesEditor.this,
                        "OWASP JSP Testing Tool", "About",
                        JOptionPane.INFORMATION_MESSAGE );
            }

        } );
        helpMenu.add( about );

        // create the menu bar and add the file and help menus
        JMenuBar menuBar = new JMenuBar();
        menuBar.add( fileMenu );
        menuBar.add( helpMenu );

        return menuBar;
    }

    /**
     * Resets the tag panel to reflect the currently selected tag. Removes all
     * items on the tag panel and creates text fields for the tag prefix, tag
     * suffix and all tag attributes. These text fields are pre-populated with
     * the current configuration values and if these values are changed, the tag
     * properties are marked as dirty
     * 
     * @param tag
     *            the currently selected tag
     */
    private void populateTagPanel( final TagInfo tag )
    {

        // remove all contents from the tag panel
        tagPanel.removeAll();

        // if no tag is selected, remove the border on the tag panel
        if ( tag == null )
        {
            tagPanel.setBorder( BorderFactory.createEmptyBorder() );
            validate();
            return;
        }

        GridBagLayout layout = (GridBagLayout) tagPanel.getLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        // each text field should fill horizontal space, expand with the window
        // and take up the entire "row"
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        /*
         * TODO: consider some kind of Factory class/method for creating the
         * text fields; also consider pooling text fields for re-use
         */

        // create the prefix text field
        final JTextArea prefix = new JTextArea();

        // pre-populate with current configured value
        prefix.setText( tagProperties.getTagPrefix( tag.getTagName() ) );

        // when focus is gained, note current value; when focus is lost, compare
        // current value to noted value and if changed, change the tag
        // properties and mark as dirty
        prefix.addFocusListener( new FocusAdapter()
        {
            private String text;

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
             */
            public void focusGained( FocusEvent e )
            {
                this.text = prefix.getText();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            public void focusLost( FocusEvent e )
            {
                String newText = prefix.getText();

                if ( ( text == null ^ newText == null )
                        || ( text != null && !text.equals( newText ) ) )
                {
                    tagProperties.setTagPrefix( tag.getTagName(), newText );
                    setDirty( true );
                }
            }

        } );

        // the text field background is white so setting the border
        // directly on the text field looks weird. create a containing box and
        // use this container for the border
        Box prefixPanel = Box.createVerticalBox();
        prefixPanel.add( prefix );

        // set the border around the containing box as prefix to show what this
        // text box is for
        prefixPanel.setBorder( BorderFactory.createTitledBorder( "prefix" ) );

        // suffix text field follows the same process as prefix
        final JTextArea suffix = new JTextArea();
        suffix.setText( tagProperties.getTagSuffix( tag.getTagName() ) );
        suffix.addFocusListener( new FocusAdapter()
        {
            private String text;

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
             */
            public void focusGained( FocusEvent e )
            {
                this.text = suffix.getText();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            public void focusLost( FocusEvent e )
            {
                String newText = suffix.getText();

                if ( ( text == null ^ newText == null )
                        || ( text != null && !text.equals( newText ) ) )
                {
                    tagProperties.setTagSuffix( tag.getTagName(), newText );
                    setDirty( true );
                }
            }

        } );

        Box suffixPanel = Box.createVerticalBox();
        suffixPanel.add( suffix );
        suffixPanel.setBorder( BorderFactory.createTitledBorder( "suffix" ) );

        layout.setConstraints( prefixPanel, constraints );
        layout.setConstraints( suffixPanel, constraints );

        // add prefix and suffix to tag panel
        tagPanel.add( prefixPanel );
        tagPanel.add( suffixPanel );

        TagAttributeInfo[] attrs = tag.getAttributes();

        // loop over each of the tag's attributes
        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            final TagAttributeInfo attr = attrs[attrIdx];

            // create a text field using same process as prefix
            final JTextField textField = new JTextField();
            textField.setText( tagProperties.getTagProperty( tag.getTagName(),
                    attr.getName() ) );
            textField.addFocusListener( new FocusAdapter()
            {
                private String text = null;

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
                 */
                public void focusGained( FocusEvent e )
                {
                    this.text = textField.getText();
                    LOGGER.fine( tag.getTagName() + "-" + attr.getName()
                            + " has value '" + text + "'" );
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
                 */
                @Override
                public void focusLost( FocusEvent e )
                {
                    String newText = textField.getText();

                    if ( ( text == null ^ newText == null )
                            || ( text != null && !text.equals( newText ) ) )
                    {
                        tagProperties.setTagProperty( tag.getTagName(), attr
                                .getName(), textField.getText() );
                        LOGGER.fine( tag.getTagName() + "-" + attr.getName()
                                + " has new value '" + newText + "'" );
                        setDirty( true );
                    }
                }

            } );

            Box panel = Box.createVerticalBox();
            panel.add( textField );
            panel.setBorder( BorderFactory.createTitledBorder( attrs[attrIdx]
                    .getName() ) );

            layout.setConstraints( panel, constraints );
            tagPanel.add( panel );
        }

        JPanel box = new JPanel();

        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        layout.setConstraints( box, constraints );

        tagPanel.add( box );

        tagPanel
                .setBorder( BorderFactory.createTitledBorder( tag.getTagName() ) );

        validate();
    }

    /**
     * Populate the list model with the tags from the currently loaded tag
     * library
     */
    private void populateTagListModel()
    {
        // remove all current elements from the list
        tagListModel.clear();

        // get the tag library's tags
        TagInfo[] tags = tldInfo.getTags();

        // iterate over all tags in library
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            // add each tag to the list model
            tagListModel.addElement( tags[tagIdx] );
        }
    }

    /**
     * Exit the application. If the tag properties configuration has changed,
     * first asks the user if they want to save changes. Also confirms that the
     * user really wants to exit the application.
     */
    private void exitApp()
    {
        // if changes have been made, prompt to see if user wants to save
        if ( dirty )
        {
            int response = JOptionPane
                    .showConfirmDialog(
                            this,
                            ( tagFile == null ? "Untitled" : tagFile.getName() )
                                    + " has unsaved changes. Do you want to save changes?",
                            "Save", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE );

            // if user wants to save, save
            if ( response == JOptionPane.YES_OPTION )
            {
                save();
            }
            // if user cancel's, stop exit
            else if ( response == JOptionPane.CANCEL_OPTION )
            {
                return;
            }
            // otherwise, user doesn't want to save and wants to exit
        }

        // prompt the user to confirm exit
        int response = JOptionPane.showConfirmDialog( this,
                "Are you sure you want to exit?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );

        // if the user says yes, exit the application
        if ( response == JOptionPane.YES_OPTION )
        {
            System.exit( 0 );
        }

    }

    /**
     * Displays an open file dialog for the user to choose the tag properties
     * configuration file
     */
    private void open()
    {
        // set file chooser's filter to tag properties configuration file
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TPX_FILE_FILTER );

        // show the file chooser
        int response = fileChooser.showOpenDialog( this );

        // if the user chooses a file, load that tag properties configuration
        // file
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            openTpxFile( fileChooser.getSelectedFile() );
        }
    }

    /**
     * Loads the given tag properties configuration file.
     * 
     * @param file
     *            the tag properties configuration file to load
     */
    private void openTpxFile( File file )
    {
        try
        {
            // load the file
            tagProperties.load( file );

            // set the current file to the newly loaded file
            tagFile = file;

            // mark as unchanged
            setDirty( false );

            // clear the tag panel and force redraw
            tagPanel.removeAll();
            tagPanel.validate();
        }
        catch ( IOException ioe )
        {
            LOGGER.throwing( this.getName(), "openTpxFile(File)", ioe );
            JOptionPane.showMessageDialog( this,
                    "Error opening properties File", "Error",
                    JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Saves the current state of the tag properties configuration to the
     * currently loaded file
     */
    private void save()
    {
        // if no file is loaded, prompt the user for a file to save to
        if ( tagFile == null )
        {
            saveAs();
        }
        // otherwise, save to current file
        else
        {

            try
            {
                // save changes
                tagProperties.save( tagFile );

                // mark as unchanged
                setDirty( false );
            }
            catch ( IOException ioe )
            {
                LOGGER.throwing( TagPropertiesEditor.class.getName(), "save",
                        ioe );
                JOptionPane.showMessageDialog( this,
                        "Error saving properties file", "Error",
                        JOptionPane.ERROR_MESSAGE );
            }
        }
    }

    /**
     * Displays a save file dialog for the user to choose the location to save
     * the tag properties configuration file to
     */
    private void saveAs()
    {

        // set file chooser's filter to tag properties configuration file
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TPX_FILE_FILTER );

        // show the file chooser
        int response = fileChooser.showSaveDialog( this );

        // if the user chooses a file, then execute save
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            // set the currently selected file
            tagFile = fileChooser.getSelectedFile();

            // if non-null, execute save
            if ( tagFile != null )
            {
                save();
            }
        }
    }

    /**
     * Displays an open file dialog for the user to choose the tag library
     * definition file to load
     */
    private void load()
    {
        // set file chooser's filter to tag library definition files
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TLD_FILE_FILTER );

        // show the file chooser
        int response = fileChooser.showOpenDialog( this );

        // if the user chooses a file, then load the TLD file
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            loadTldFile( fileChooser.getSelectedFile() );
        }

    }

    /**
     * Marks the GUI indicating whether or not the tag properties has changed.
     * This alters the title of the window.
     * 
     * @param dirty
     *            whether or not the tag properties has changed
     */
    private void setDirty( boolean dirty )
    {
        /*
         * TODO: really should have a property change listener on the
         * TagProperties class and this class as a listener to determine dirty
         * status
         */
        this.dirty = dirty;

        if ( dirty )
        {
            setTitle( TITLE + " - "
                    + ( tagFile == null ? "Untitled" : tagFile.getName() )
                    + " (*)" );
        }
        else
        {
            setTitle( TITLE + " - "
                    + ( tagFile == null ? "Untitled" : tagFile.getName() ) );
        }
    }

    /**
     * Loads the given TLD file.
     * 
     * @param tldFile
     *            the TLD file to load
     */
    private void loadTldFile( File tldFile )
    {
        // do sanity checking to ensure the file exists and is a file
        if ( !tldFile.exists() || tldFile.exists() && !tldFile.isFile() )
        {
            JOptionPane.showMessageDialog( this,
                    "The specified TLD file does not exist", "Error",
                    JOptionPane.ERROR_MESSAGE );
        }

        try
        {
            // load the TLD file
            tldInfo = TagFileParser.loadTagFile( tldFile );

            // populate the list of tags from the new tag library
            populateTagListModel();
        }
        catch ( IOException ioe )
        {
            LOGGER.throwing( this.getName(), "loadTldFile(File)", ioe );
            JOptionPane.showMessageDialog( this, "Error Loading TLD File",
                    "Error", JOptionPane.ERROR_MESSAGE );
            return;
        }
        catch ( SAXException se )
        {
            LOGGER.throwing( this.getName(), "loadTldFile(File)", se );
            JOptionPane.showMessageDialog( this, "Error Parsing TLD File",
                    "Error", JOptionPane.ERROR_MESSAGE );

            return;
        }
    }

    /**
     * Launches the GUI based on the optional command line arguments:
     * <ol>
     * <li>tag properties configuration file </li>
     * <li>tag library file</li>
     * </ol>
     * 
     * @param args
     *            command line arguments
     */
    public static void main( String[] args )
    {
        TagPropertiesEditor tpe = new TagPropertiesEditor();

        // if first argument is present, load the given tag properties
        // configuration file
        if ( args.length > 0 )
        {
            tpe.openTpxFile( new File( args[0] ) );
        }

        // if the second argument is present, load the given tag library file
        if ( args.length > 1 )
        {
            tpe.loadTldFile( new File( args[1] ) );
        }

        // display the GUI
        tpe.setVisible( true );
    }
}
