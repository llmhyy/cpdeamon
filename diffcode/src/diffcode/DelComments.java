/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffcode;

/**
 *
 * @author sxz
 */
public class DelComments {
	private static final char MARK = '"';

	private static final char SLASH = '/';

	private static final char BACKSLASH = '\\';

	private static final char STAR = '*';

	private static final char NEWLINE = '\n';

	// ����
	private static final int TYPE_MARK = 1;

	// б��
	private static final int TYPE_SLASH = 2;

	// ��б��
	private static final int TYPE_BACKSLASH = 3;

	// �Ǻ�
	private static final int TYPE_STAR = 4;

	// ˫б�����͵�ע��
	private static final int TYPE_DSLASH = 5;
	// б����
	private static final int TYPE_SLASH_STAR = 6;

	/**
	 * ɾ��char[]������_startλ�õ�_endλ�õ�Ԫ��
	 * 
	 * @param _target
	 * @param _start
	 * @param _end
	 * @return
	 */
	public static char[] del(char[] _target, int _start, int _end) {
		char[] tmp = new char[_target.length - (_end - _start + 1)];
		System.arraycopy(_target, 0, tmp, 0, _start);
		System.arraycopy(_target, _end + 1, tmp, _start, _target.length - _end
				- 1);
		return tmp;
	}

	/**
	 * ɾ�������е�ע��
	 * 
	 * @param _target
	 * @return
	 */
	public static String delComments(String _target) {
		int preType = 0;
		boolean flag = false;
		boolean flag2 = false;
		int mark = -1, cur = -1, token = -1;
		// �����ַ���
		char[] input = _target.toCharArray();
		for (cur = 0; cur < input.length; cur++) {
			if (input[cur] == MARK && !flag && !flag2) {
				// �����ж��Ƿ�Ϊת������
				if (preType == TYPE_BACKSLASH)
					continue;
				// �Ѿ���������֮��
				if (mark > 0) {
					// ���Ž���
					mark = -1;
				} else {
					mark = cur;
				}
				preType = TYPE_MARK;
			} else if (input[cur] == SLASH && !flag2) {
				// ��ǰλ�ô�������֮��
				if (mark > 0 && !flag)
					continue;
				// ���ǰһλ��*�������ɾ������
				if (preType == TYPE_STAR) {
					input = del(input, token, cur);
					// �˻�һ��λ�ý��д���
					cur = token - 1;
					preType = 0;
					flag = false;
				} else if (preType == TYPE_SLASH && !flag) {
					token = cur - 1;
					preType = TYPE_DSLASH;
					flag2 = true;
				} else {
					preType = TYPE_SLASH;
				}
			} else if (input[cur] == BACKSLASH && !flag && !flag2) {
				preType = TYPE_BACKSLASH;
			} else if (input[cur] == STAR && !flag2) {
				// ��ǰλ�ô�������֮��
				if (mark > 0 && !flag)
					continue;
				// ���ǰһ��λ����/,���¼ע�Ϳ�ʼ��λ��
				if (preType == TYPE_SLASH && !flag) {
					token = cur - 1;
					preType = 0;
					flag = true;
				} else
					preType = TYPE_STAR;
			} else if (input[cur] == NEWLINE) {
				if (preType == TYPE_DSLASH || flag2) {
					input = del(input, token, cur - 1);
					// �˻�һ��λ�ý��д���
					cur = token - 1;
					flag2 = false;
				}
				preType = 0;
			} else
				preType = 0;
		}
		return new String(input);
        }
}
