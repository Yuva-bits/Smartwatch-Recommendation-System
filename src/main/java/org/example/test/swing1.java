package org.example.test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class swing1 {
    static class fr5 extends JFrame implements ActionListener
    {
        JButton bt;
        JTextField tf;
        fr5()
        {
            setLayout(null);
            tf = new JTextField();
            bt = new JButton("This is Button");

            tf.setBounds(100,100,200,30);
            bt.setBounds(300,300,150,150);

            add(tf);
            add(bt);

            //Register bt with Action Listener
            bt.addActionListener(this);

            setDefaultCloseOperation(EXIT_ON_CLOSE);

            setSize(500, 500);
            setVisible(true);
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            //Write your logic here
            tf.setText(Math.random()+"");
        }


    }
    public static void main(String[] args)
    {
        fr5 obj = new fr5();
    }
}
