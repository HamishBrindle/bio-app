package com.biomap.application.bio_app.OpenGL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import android.opengl.GLES20;
import android.util.Log;

public class GLHeatmap {

	private static final String TAG = "GLHeatmap";
	/** Width of the viewport */
	private Integer width;
	/** Height of the viewport */
	private Integer height;
	/** Array which stores the generated buffer object names */
	private int[] quad;
	/** A Heights */
	private Heights heights;
	/** A Shader */
	private Shader shader;

	/**
	 * Create a GLHeatmap object.
	 *
	 * @param width Width of the viewport
	 * @param height Height of the viewport
	 * @param intensityToAlpha Not in use
	 * @param gradientTexture Not in use
	 * @param alphaRange Not in use
	 * @throws Exception
	 */
	public GLHeatmap(final int width, final int height, Boolean intensityToAlpha, Object gradientTexture, float[] alphaRange) throws Exception {

		this.width = width;
		this.height = height;

		// ...
		GLES20.glEnableVertexAttribArray(Main.BIND_ZERO);
		//MyGLRenderer.checkGlError("glEnableVertexAttribArray");
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
		//MyGLRenderer.checkGlError("glBlendFunc");
// ...
		String getColorFun  =
			"vec3 getColor(float intensity){						\n" +
			"    vec3 blue   = vec3(0.5, 0.0, 1.0);					\n" +
			"    vec3 cyan   = vec3(0.5, 1.0, 1.0);					\n" +
			"    vec3 green  = vec3(0.5, 1.0, 0.5);	 				\n" +
			"    vec3 yellow = vec3(1.0, 1.0, 0.5);					\n" +
			"    vec3 red    = vec3(1.0, 0.5, 0.5);					\n" +
			"    \n" +
			"    vec3 color = (\n" +
			"        fade(-0.25, 0.25, intensity)*blue +			\n" +
			"        fade(  0.0,  0.5, intensity)*cyan +			\n" +
			"        fade( 0.25, 0.75, intensity)*green +			\n" +
			"        fade(  0.5,  1.0, intensity)*yellow +			\n" +
			"        smoothstep(0.75, 1.0, intensity)*red			\n" +
			"    );													\n" +
			"    return color;										\n" +
			"}";

		String output =
			"vec4 alphaFun(vec3 color, float intensity){			\n" +
			"    float alpha = smoothstep(0.0, 1.0, intensity);		\n" +
			"    return vec4(color*alpha, alpha);					\n" +
			"}";

		this.shader = new Shader(Main.vertexShaderBlit, Main.fragmentShaderBlit +

			"float fade(float low, float high, float value){		\n" +
			"    float mid   = (low+high)*0.5;						\n" +
			"    float range = (high-low)*0.5;						\n" +
			"    float x = 1.0 - clamp(abs(mid-value)/range, 0.0, 1.0);\n" +
			"    return smoothstep(0.0, 1.0, x);					\n" +
			"}\n" +
			"\n" + getColorFun + "\n" + output + "\n" +
			"\n" +
			"void main(){\n" +
			"    float intensity = smoothstep(0.0, 1.0, texture2D(source, texcoord).r);\n" +
			"    vec3 color = getColor(intensity);\n" +
			"    gl_FragColor = alphaFun(color, intensity);\n" +
			"}");

		GLES20.glViewport(0, 0, this.width, this.height);
		this.quad = new int[Main.NUM_BUFFER];
		GLES20.glGenBuffers(Main.NUM_BUFFER, this.quad, Main.BUFFER_OFFSET);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.quad[Main.BUFFER_OFFSET]);
		float[] tmp = new float[]{-1, -1, 0, 1, 1, -1, 0, 1, -1, 1, 0, 1, -1, 1, 0, 1, 1, -1, 0, 1, 1, 1, 0, 1};
		FloatBuffer quad_l = ByteBuffer.allocateDirect(tmp.length * Main.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		quad_l.put(tmp, 0, tmp.length);
		quad_l.position(0);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, quad_l.capacity() * Main.BYTES_PER_FLOAT, quad_l, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Main.BUFFER_NULL);
		this.heights = new Heights(this, this.width, this.height);
	}

	/**
	 * Set a new size.
	 *
	 * @param width Width of the viewport.
	 * @param height Height of the viewport.
	 */
	public void adjustSize(int width, int height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			GLES20.glViewport(0, 0, this.width, this.height);
			this.heights.resize(this.width, this.height);
		}
	}

	/**
	 * Display the heatmap.
	 */
	public void display() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.quad[Main.BUFFER_OFFSET]);
		GLES20.glVertexAttribPointer(Main.BIND_ZERO, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, Main.BUFFER_OFFSET);
		this.heights.nodeFront.bind(Main.BIND_ZERO);
		this.shader.use()._int(Main.VARIABLE_UNIFORM_SOURCE, Main.BIND_ZERO);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Main.NUM_INDICES_RENDER);
	}

	/**
	 * Update the {@link Heights} object by call {@link Heights#update()}.
	 */
	public void update() {
		this.heights.update();
	}

	/**
	 * Clear the {@link Heights} by call {@link Heights#clear()}.
	 */
	public void clear() {
		this.heights.clear();
	}

	/* (non-Javadoc)
	 *
	 * @see Heights#addPoint(float, float, float, float);
	 */
	public void addPoint(float x, float y, float size, float intensity) {
		this.heights.addPoint(x, y, size, intensity);
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public void plotHeatMap() {

		int[][] pressure = convert2DArray(getPressure());

		float intensity;
		float radius = 400;

		int numOfRow = pressure.length + 2;
		int numOfCol = pressure[0].length + 2;

		float rowSize = getHeight() / numOfRow; // 131
		float colSize = getWidth() / numOfCol; // 166

		float rowOffset = rowSize / 2f;
		float colOffset = colSize / 2f;

		int index = 0;

		// rows and columns = 10
		for (int yPos = 0; yPos < numOfRow + 1; yPos++) {
			for (int xPos = 0; xPos < numOfCol + 1; xPos++) {
				if ((yPos > 0 && yPos < numOfRow - 1) && (xPos > 0 && xPos < numOfCol - 1)) {
					intensity = pressure[xPos - 1][yPos - 1] / 100.0f;
					addPoint((xPos * colSize) + colOffset, (yPos * rowSize) + rowOffset, radius, intensity);
					Log.d(TAG, "plotHeatMap: Adding point.");
				} else {
					Log.d(TAG, "plotHeatMap: Skipping point.");
				}

			}
		}
	}

	private int[] getRandomPressure() {
		int[] array = new int[64];
		Random rand = new Random();

		for (int i = 0; i < 64; i++)
			array[i] = rand.nextInt(50);

		return array;
	}

	/**
	 * Gets a pressure-reading array of values for each node.
	 *
	 * @return The pressure map array of values.
	 */
	private int[] getPressure() {

		// TODO: This will eventually get information from the nodes and create an array.

		return new int[]{
				5 , 25, 45, 50, 60, 45, 25, 5 ,
				10, 20, 12, 20, 20, 12, 20, 10,
				15, 30, 15, 17, 19, 15, 30, 15,
				20, 40, 20, 10, 10, 20, 40, 20,
				20, 40, 20, 0 , 0 , 20, 40, 20,
				15, 30, 15, 0 , 0 , 15, 30, 15,
				10, 20, 10, 0 , 0 , 10, 20, 10,
				15, 30, 15, 0 , 0 , 15, 30, 15
		};
	}

	/**
	 * Convert a 1D matrix into a
	 *
	 * @param input Array to be converted.
	 * @return Converted array.
	 */
	private int[][] convert2DArray(int[] input) {
		int resolution = (int) Math.sqrt(input.length);
		int[][] output = new int[resolution][resolution];
		int count = 0;

		for (int yPos = 0; yPos < resolution; yPos++) {
			for (int xPos = 0; xPos < resolution; xPos++) {
				output[xPos][yPos] = input[count++];
			}
		}
		return output;
	}

}
