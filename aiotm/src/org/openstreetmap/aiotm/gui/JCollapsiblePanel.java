package org.openstreetmap.aiotm.gui;

import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.aiotm.util.GBC;

public class JCollapsiblePanel extends JPanel {

	private boolean expanded;
	JPanel contentPanel_;
	HeaderPanel headerPanel_;

	private class HeaderPanel extends JPanel {
		JLabel title;
		public void toggle() {
			expanded = !expanded;

			if (contentPanel_.isShowing()) {
				contentPanel_.setVisible(false);
				title.setIcon(new ImageIcon(ClassLoader.getSystemResource("images/minimized.png")));
			}
			else {
				contentPanel_.setVisible(true);
				title.setIcon(new ImageIcon(ClassLoader.getSystemResource("images/normal.png")));
			}
			validate();

			headerPanel_.repaint();
		}

		public HeaderPanel(String text) {

			setLayout(new GridBagLayout());
			title = new JLabel(text,new ImageIcon(ClassLoader.getSystemResource("images/minimized.png")),SwingConstants.LEADING);
			add(title,GBC.eol());


			addMouseListener(
					new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							toggle();
						}
					}
			);

		}

	}

	public JCollapsiblePanel(String text, JPanel panel) {
		super(new GridBagLayout());
		expanded = false;
		headerPanel_ = new HeaderPanel(text);
		contentPanel_ = panel;
		add(headerPanel_, GBC.eol());
		add(contentPanel_, GBC.eol());
		contentPanel_.setVisible(false);
	}

	public void collapse() {
		if (expanded) {
			headerPanel_.toggle();
		}
	}

	public void expand() {
		if (!expanded) {
			headerPanel_.toggle();
		}
	}

	public JPanel getContentPanel() {
		return contentPanel_;
	}
}