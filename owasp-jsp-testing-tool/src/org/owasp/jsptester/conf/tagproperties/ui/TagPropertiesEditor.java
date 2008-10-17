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
 * @author Jason Li
 * 
 */
public class TagPropertiesEditor extends JFrame
{

    private static final Logger LOGGER = Logger
            .getLogger( TagPropertiesEditor.class.getName() );

    private static final String TITLE = "OWASP JSP Testing Tool - Tag Properties Editor";

    private static final FileFilter TLD_FILE_FILTER = new FileFilter()
    {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept( File f )
        {
            return f.isDirectory() || f.getName().endsWith( ".tld" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Tag Library Description file";
        }

    };

    private static final FileFilter TPX_FILE_FILTER = new FileFilter()
    {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept( File f )
        {
            return f.isDirectory() || f.getName().endsWith( ".tpx" );
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Tag Properties XML file";
        }

    };

    private TagLibraryInfo tldInfo = null;

    private File tagFile = null;

    private boolean dirty = false;

    private final TagProperties tagProperties = new TagProperties();

    private final DefaultListModel tagListModel = new DefaultListModel();

    private final JPanel tagPanel = new JPanel();

    private final JFileChooser fileChooser = new JFileChooser();

    public TagPropertiesEditor()
    {
        super( TITLE );

        setJMenuBar( buildMenuBar() );

        final JList tagList = new JList( tagListModel );
        tagList.setCellRenderer( new TagInfoListCellRenderer( tagList
                .getCellRenderer() ) );
        tagList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
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

        tagPanel.setLayout( new GridBagLayout() );

        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        splitPane.setLeftComponent( new JScrollPane( tagList ) );
        splitPane.setRightComponent( new JScrollPane( tagPanel ) );

        getContentPane().add( splitPane );

        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            @Override
            public void windowClosing( WindowEvent e )
            {
                exitApp();

            }

        } );

        setSize( 425, 300 );
    }

    private JMenuBar buildMenuBar()
    {
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

        JMenu fileMenu = new JMenu( "File" );
        fileMenu.add( open );
        fileMenu.add( save );
        fileMenu.add( saveAs );
        fileMenu.addSeparator();
        fileMenu.add( load );
        fileMenu.addSeparator();
        fileMenu.add( exit );

        JMenu helpMenu = new JMenu( "Help" );
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

            }

        } );
        helpMenu.add( about );

        JMenuBar menuBar = new JMenuBar();
        menuBar.add( fileMenu );
        menuBar.add( helpMenu );

        return menuBar;
    }

    private void populateTagPanel( final TagInfo tag )
    {

        tagPanel.removeAll();

        if ( tag == null )
        {
            tagPanel.setBorder( BorderFactory.createEmptyBorder() );
            validate();
            return;
        }

        GridBagLayout layout = (GridBagLayout) tagPanel.getLayout();

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        final JTextArea prefix = new JTextArea();
        prefix.setText( tagProperties.getTagPrefix( tag.getTagName() ) );
        prefix.addFocusListener( new FocusAdapter()
        {
            private String text;

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
             */
            @Override
            public void focusGained( FocusEvent e )
            {
                this.text = prefix.getText();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            @Override
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

        Box prefixPanel = Box.createVerticalBox();
        prefixPanel.add( prefix );
        prefixPanel.setBorder( BorderFactory.createTitledBorder( "prefix" ) );

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
            @Override
            public void focusGained( FocusEvent e )
            {
                this.text = suffix.getText();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            @Override
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

        tagPanel.add( prefixPanel );
        tagPanel.add( suffixPanel );

        TagAttributeInfo[] attrs = tag.getAttributes();

        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            final TagAttributeInfo attr = attrs[attrIdx];

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
                @Override
                public void focusGained( FocusEvent e )
                {
                    this.text = textField.getText();
                    LOGGER.info( tag.getTagName() + "-" + attr.getName()
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
                        LOGGER.info( tag.getTagName() + "-" + attr.getName()
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

    private void populateTagListModel()
    {
        tagListModel.clear();

        TagInfo[] tags = tldInfo.getTags();
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            tagListModel.addElement( tags[tagIdx] );
        }
    }

    private void exitApp()
    {
        if ( dirty )
        {
            int response = JOptionPane
                    .showConfirmDialog(
                            this,
                            ( tagFile == null ? "Untitled" : tagFile.getName() )
                                    + " has unsaved changes. Do you want to save changes?",
                            "Save", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE );

            if (response == JOptionPane.YES_OPTION) {
                save();
            } else if (response == JOptionPane.CANCEL_OPTION ) {
                return;
            }
        }

        int response = JOptionPane.showConfirmDialog( this,
                "Are you sure you want to exit?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );

        if ( response == JOptionPane.YES_OPTION )
        {
            System.exit( 0 );
        }

    }

    private void open()
    {
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TPX_FILE_FILTER );
        int response = fileChooser.showOpenDialog( this );
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            try
            {
                tagFile = fileChooser.getSelectedFile();
                tagProperties.load( tagFile );
                setDirty( false );
            }
            catch ( IOException ioe )
            {
                LOGGER.throwing( TagPropertiesEditor.class.getName(), "open",
                        ioe );
                JOptionPane.showMessageDialog( this,
                        "Error opening properties file", "Error",
                        JOptionPane.ERROR_MESSAGE );
            }
        }

        tagPanel.removeAll();
        tagPanel.validate();
    }

    private void save()
    {
        if ( tagFile == null )
        {
            saveAs();
        }
        else
        {

            try
            {
                tagProperties.save( tagFile );
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

    private void saveAs()
    {
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TPX_FILE_FILTER );
        int response = fileChooser.showSaveDialog( this );
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            tagFile = fileChooser.getSelectedFile();
            if ( tagFile != null )
            {
                save();
                setDirty( false );
            }
        }
    }

    private void load()
    {
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter( TLD_FILE_FILTER );
        int response = fileChooser.showOpenDialog( this );
        if ( response == JFileChooser.APPROVE_OPTION )
        {
            loadTldFile( fileChooser.getSelectedFile() );
        }

    }

    private void setDirty( boolean dirty )
    {
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

    private void loadTldFile( File tldFile )
    {
        if ( !tldFile.exists() || tldFile.exists() && !tldFile.isFile() )
        {
            JOptionPane.showMessageDialog( this,
                    "The specified TLD file does not exist", "Error",
                    JOptionPane.ERROR_MESSAGE );
        }

        try
        {
            tldInfo = TagFileParser.loadTagFile( tldFile );
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

        populateTagListModel();
    }

    public static void main( String[] args )
    {
        TagPropertiesEditor tpe = new TagPropertiesEditor();
        tpe.loadTldFile( new File( "./resources/html_basic.tld" ) );

        tpe.setVisible( true );
    }
}
