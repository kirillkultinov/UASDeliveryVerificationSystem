//
//  UserManualViewController.swift
//  UAS Delivery
//
//  Created by Kirill Kultinov on 4/9/17.
//  Copyright © 2017 Kirill Kultinov. All rights reserved.
//

import UIKit

class UserManualViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var userManualTable: UITableView!
    
    let userManualItems = ["Step 1:", "Login into your account on Delivery Status page. You will see delivery status of your package along with Drone's name that deliveres your package and a verification code that will be used for pairing the drone with your device.", "Step 2:", "When package has arrived, remember the verification code provided inside Delivery Status tab.", "Step 3:", "Press Pair button in order to open Bluetooth settings. Choose BLuetooth device that has Drone's name. Use the verification code for pairing", "Step 4:", "Go back to the application. Now verify button will be enabled.", "Step 5:", "Press verify button. Wait a couple of seconds until the delivery of your package is verified."]
    
    override func viewDidLoad() {
        super.viewDidLoad()

        userManualTable.delegate = self
        userManualTable.dataSource = self
        userManualTable.rowHeight = UITableViewAutomaticDimension
        userManualTable.estimatedRowHeight = 150
        
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func tableView(_ tableView:UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return userManualItems.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell{
        let cell : UITableViewCell = tableView.dequeueReusableCell(withIdentifier: "userManualCell", for: indexPath as IndexPath) as UITableViewCell
        
        cell.textLabel?.text = self.userManualItems[indexPath.row]
        
        if(!((cell.textLabel?.text?.contains(":"))!)){
            cell.textLabel?.textColor = UIColor.gray
        }
        
        return cell
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
