/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

/*
 *
 * @author Administrator
 */
public  class Solution2{
    /*
     * 
     * @param ��������
     * @param target
     * @return 
     */
    //sjoapja
    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] == target - nums[i]) {
                    return new int[] { i, j };//�þ�û����ʲô��ɫ��
                }
            }
        }
        throw new IllegalArgumentException("No two sum solution");
    }
}
